package com.resilire.backend.prescription;

import com.resilire.backend.common.exception.ResourceNotFoundException;
import com.resilire.backend.consultation.Consultation;
import com.resilire.backend.consultation.ConsultationService;
import com.resilire.backend.consultation.ConsultationStatus;
import com.resilire.backend.prescription.dto.DownloadableFile;
import com.resilire.backend.prescription.dto.MedicineItem;
import com.resilire.backend.prescription.dto.PrescriptionResponse;
import com.resilire.backend.prescription.dto.SavePrescriptionDraftRequest;
import com.resilire.backend.prescription.dto.TestItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Release 5 - Prescription & Consultation History. A prescription is either doctor-authored
 * (GENERATED, built up as a DRAFT via medicines/tests and locked by "Finalize prescription",
 * which also completes the {@link Consultation}) or a patient-supplied scanned/photographed
 * document (OFFLINE_UPLOAD, created directly in FINALIZED status and completing the
 * consultation immediately, since there is no doctor review step for it).
 */
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationService consultationService;
    private final PrescriptionPdfService prescriptionPdfService;
    private final PrescriptionFileStorageService fileStorageService;

    /**
     * Read-only: does NOT persist an empty draft row just because the doctor viewed the
     * prescription tab. {@code prescriptions.consultation_id} is unique, so eagerly creating a
     * row here would permanently claim that slot and could block a patient's later offline
     * upload for the same consultation even though the doctor never actually wrote anything.
     * A real row is only created once the doctor explicitly saves ({@link #saveDraft}).
     */
    @Transactional(readOnly = true)
    public PrescriptionResponse getOrCreateDraftForDoctor(Long doctorUserId, Long consultationId) {
        Consultation consultation = consultationService.getEntityOwnedByDoctor(doctorUserId, consultationId);
        return prescriptionRepository.findByConsultationId(consultationId)
                .map(this::toResponse)
                .orElseGet(() -> emptyDraftResponse(consultation));
    }

    @Transactional
    public PrescriptionResponse saveDraft(
            Long doctorUserId, Long consultationId, SavePrescriptionDraftRequest request) {
        Consultation consultation = consultationService.getEntityOwnedByDoctor(doctorUserId, consultationId);
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new IllegalArgumentException("error.consultation.locked");
        }
        Prescription prescription = getOrCreateDraft(consultation);
        if (prescription.getStatus() == PrescriptionStatus.FINALIZED) {
            throw new IllegalArgumentException("error.prescription.locked");
        }

        prescription.setNotes(request.getNotes());

        prescription.getMedicines().clear();
        int order = 0;
        for (MedicineItem item : request.getMedicines()) {
            prescription.getMedicines().add(PrescriptionMedicine.builder()
                    .prescription(prescription)
                    .medicineName(item.getMedicineName())
                    .dosage(item.getDosage())
                    .frequency(item.getFrequency())
                    .duration(item.getDuration())
                    .instructions(item.getInstructions())
                    .sortOrder(order++)
                    .build());
        }

        prescription.getTests().clear();
        order = 0;
        for (TestItem item : request.getTests()) {
            prescription.getTests().add(PrescriptionTest.builder()
                    .prescription(prescription)
                    .testName(item.getTestName())
                    .instructions(item.getInstructions())
                    .sortOrder(order++)
                    .build());
        }

        return toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional
    public PrescriptionResponse finalizePrescription(Long doctorUserId, Long consultationId) {
        Consultation consultation = consultationService.getEntityOwnedByDoctor(doctorUserId, consultationId);
        Prescription prescription = getExisting(consultationId);
        if (prescription.getStatus() == PrescriptionStatus.FINALIZED) {
            throw new IllegalArgumentException("error.prescription.alreadyFinalized");
        }
        if (prescription.getMedicines().isEmpty() && prescription.getTests().isEmpty()) {
            throw new IllegalArgumentException("error.prescription.empty");
        }

        prescription.setStatus(PrescriptionStatus.FINALIZED);
        prescription.setFinalizedAt(LocalDateTime.now());
        prescriptionRepository.save(prescription);
        consultationService.markCompleted(consultation);

        return toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getForPatient(Long patientUserId, Long consultationId) {
        consultationService.getEntityOwnedByPatient(patientUserId, consultationId);
        return toResponse(getFinalizedOrThrow(consultationId));
    }

    @Transactional
    public PrescriptionResponse uploadOffline(
            Long patientUserId, Long appointmentId, MultipartFile file, String notes) {
        Consultation consultation =
                consultationService.getOrCreateForAppointmentAsPatient(patientUserId, appointmentId);
        if (prescriptionRepository.findByConsultationId(consultation.getId()).isPresent()) {
            throw new IllegalArgumentException("error.prescription.alreadyExists");
        }

        StoredFile stored = fileStorageService.store(file);
        Prescription prescription = Prescription.builder()
                .consultation(consultation)
                .type(PrescriptionType.OFFLINE_UPLOAD)
                .status(PrescriptionStatus.FINALIZED)
                .notes(notes)
                .fileName(stored.originalFileName())
                .fileContentType(stored.contentType())
                .fileSize(stored.size())
                .fileStoragePath(stored.storagePath())
                .finalizedAt(LocalDateTime.now())
                .build();

        return toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public DownloadableFile downloadForDoctor(Long doctorUserId, Long consultationId) {
        consultationService.getEntityOwnedByDoctor(doctorUserId, consultationId);
        return buildDownload(getExisting(consultationId));
    }

    @Transactional(readOnly = true)
    public DownloadableFile downloadForPatient(Long patientUserId, Long consultationId) {
        consultationService.getEntityOwnedByPatient(patientUserId, consultationId);
        return buildDownload(getFinalizedOrThrow(consultationId));
    }

    @Transactional(readOnly = true)
    public DownloadableFile downloadCertificateForDoctor(Long doctorUserId, Long consultationId) {
        Consultation consultation = consultationService.getEntityOwnedByDoctor(doctorUserId, consultationId);
        return new DownloadableFile(prescriptionPdfService.generateCertificate(consultation), "application/pdf",
                "medical-certificate-" + consultationId + ".pdf");
    }

    private DownloadableFile buildDownload(Prescription prescription) {
        if (prescription.getType() == PrescriptionType.GENERATED) {
            byte[] pdf = prescriptionPdfService.generate(prescription);
            return new DownloadableFile(pdf, "application/pdf", "prescription-" + prescription.getId() + ".pdf");
        }
        byte[] content = fileStorageService.load(prescription.getFileStoragePath());
        return new DownloadableFile(content, prescription.getFileContentType(), prescription.getFileName());
    }

    private PrescriptionResponse emptyDraftResponse(Consultation consultation) {
        return PrescriptionResponse.builder()
                .id(null)
                .consultationId(consultation.getId())
                .type(PrescriptionType.GENERATED)
                .status(PrescriptionStatus.DRAFT)
                .notes(null)
                .medicines(List.of())
                .tests(List.of())
                .build();
    }

    private Prescription getOrCreateDraft(Consultation consultation) {
        return prescriptionRepository.findByConsultationId(consultation.getId())
                .orElseGet(() -> prescriptionRepository.save(Prescription.builder()
                        .consultation(consultation)
                        .type(PrescriptionType.GENERATED)
                        .status(PrescriptionStatus.DRAFT)
                        .build()));
    }

    private Prescription getExisting(Long consultationId) {
        return prescriptionRepository.findByConsultationId(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("error.prescription.notFound"));
    }

    private Prescription getFinalizedOrThrow(Long consultationId) {
        Prescription prescription = getExisting(consultationId);
        if (prescription.getStatus() != PrescriptionStatus.FINALIZED) {
            throw new ResourceNotFoundException("error.prescription.notFound");
        }
        return prescription;
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        List<MedicineItem> medicines = prescription.getMedicines().stream()
                .map(m -> new MedicineItem(
                        m.getMedicineName(), m.getDosage(), m.getFrequency(), m.getDuration(), m.getInstructions()))
                .toList();
        List<TestItem> tests = prescription.getTests().stream()
                .map(t -> new TestItem(t.getTestName(), t.getInstructions()))
                .toList();

        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .consultationId(prescription.getConsultation().getId())
                .type(prescription.getType())
                .status(prescription.getStatus())
                .notes(prescription.getNotes())
                .medicines(medicines)
                .tests(tests)
                .fileName(prescription.getFileName())
                .fileContentType(prescription.getFileContentType())
                .fileSize(prescription.getFileSize())
                .createdAt(prescription.getCreatedAt())
                .finalizedAt(prescription.getFinalizedAt())
                .build();
    }
}
