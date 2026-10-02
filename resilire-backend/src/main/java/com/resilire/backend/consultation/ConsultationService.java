package com.resilire.backend.consultation;

import com.resilire.backend.appointment.Appointment;
import com.resilire.backend.appointment.AppointmentRepository;
import com.resilire.backend.appointment.AppointmentStatus;
import com.resilire.backend.common.exception.ResourceNotFoundException;
import com.resilire.backend.consultation.dto.ConsultationResponse;
import com.resilire.backend.consultation.dto.UpdateConsultationDetailsRequest;
import com.resilire.backend.doctor.DoctorProfile;
import com.resilire.backend.doctor.DoctorProfileRepository;
import com.resilire.backend.patient.PatientProfile;
import com.resilire.backend.patient.PatientProfileRepository;
import com.resilire.backend.prescription.Prescription;
import com.resilire.backend.prescription.PrescriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Release 5 - Prescription & Consultation History. A consultation is the digital
 * record a doctor opens against a booked appointment; medicines/tests/generation are
 * handled by {@link com.resilire.backend.prescription.PrescriptionService}, which
 * reuses the entity-returning helpers here for ownership checks.
 */
@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;

    @Transactional
    public ConsultationResponse openConsultation(Long doctorUserId, Long appointmentId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.appointment.notFound"));
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("error.appointment.forbidden");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("error.consultation.appointmentCancelled");
        }

        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> consultationRepository.save(
                        Consultation.builder()
                                .appointment(appointment)
                                .status(ConsultationStatus.OPEN)
                                .build()));

        return toResponse(consultation);
    }

    @Transactional
    public ConsultationResponse updateDetails(
            Long doctorUserId, Long consultationId, UpdateConsultationDetailsRequest request) {
        Consultation consultation = getEntityOwnedByDoctor(doctorUserId, consultationId);
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new IllegalArgumentException("error.consultation.locked");
        }
        consultation.setChiefComplaint(request.getChiefComplaint());
        consultation.setDiagnosis(request.getDiagnosis());
        consultation.setNotes(request.getNotes());
        consultation.setMedicalHistory(request.getMedicalHistory());
        consultation.setSurgicalHistory(request.getSurgicalHistory());
        consultation.setPsychiatricHistory(request.getPsychiatricHistory());
        consultation.setFamilyHistory(request.getFamilyHistory());
        consultation.setTobaccoUse(request.getTobaccoUse());
        consultation.setTobaccoDetails(request.getTobaccoDetails());
        consultation.setAlcoholUse(request.getAlcoholUse());
        consultation.setAlcoholDetails(request.getAlcoholDetails());
        consultation.setDrugUse(request.getDrugUse());
        consultation.setDrugDetails(request.getDrugDetails());
        consultation.setAnamnesis(request.getAnamnesis());
        consultation.setMentalExam(request.getMentalExam());
        consultation.setCie10Diagnosis(request.getCie10Diagnosis());
        consultation.setIndications(request.getIndications());
        consultation.setCertificateReason(request.getCertificateReason());
        return toResponse(consultationRepository.save(consultation));
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> listForDoctor(Long doctorUserId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));
        return consultationRepository.findByAppointmentDoctorIdOrderByCreatedAtDesc(doctor.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> listForPatient(Long patientUserId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));
        return consultationRepository.findByAppointmentPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream().map(this::toPatientResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getForDoctor(Long doctorUserId, Long consultationId) {
        return toResponse(getEntityOwnedByDoctor(doctorUserId, consultationId));
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getForPatient(Long patientUserId, Long consultationId) {
        return toPatientResponse(getEntityOwnedByPatient(patientUserId, consultationId));
    }

    @Transactional
    public Consultation getOrCreateForAppointmentAsPatient(Long patientUserId, Long appointmentId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.appointment.notFound"));
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("error.appointment.forbidden");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("error.consultation.appointmentCancelled");
        }

        return consultationRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> consultationRepository.save(
                        Consultation.builder()
                                .appointment(appointment)
                                .status(ConsultationStatus.COMPLETED)
                                .finalizedAt(LocalDateTime.now())
                                .build()));
    }

    /**
     * Marks the consultation COMPLETED and, if the appointment is still SCHEDULED,
     * marks it COMPLETED too - called once the prescription is finalized.
     */
    @Transactional
    public void markCompleted(Consultation consultation) {
        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultation.setFinalizedAt(LocalDateTime.now());
        consultationRepository.save(consultation);

        Appointment appointment = consultation.getAppointment();
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }
    }

    public Consultation getEntityOwnedByDoctor(Long doctorUserId, Long consultationId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("error.consultation.notFound"));
        if (!consultation.getAppointment().getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("error.consultation.forbidden");
        }
        return consultation;
    }

    public Consultation getEntityOwnedByPatient(Long patientUserId, Long consultationId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("error.consultation.notFound"));
        if (!consultation.getAppointment().getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("error.consultation.forbidden");
        }
        return consultation;
    }

    private ConsultationResponse toResponse(Consultation consultation) {
        return buildResponse(consultation, false);
    }

    /**
     * Doctor-authored notes/diagnosis and any not-yet-finalized prescription are
     * withheld from the patient while the consultation is still OPEN - a draft
     * in-progress note/prescription shouldn't be visible until the doctor finalizes it.
     */
    private ConsultationResponse toPatientResponse(Consultation consultation) {
        return buildResponse(consultation, true);
    }

    private ConsultationResponse buildResponse(Consultation consultation, boolean isPatientView) {
        Appointment appointment = consultation.getAppointment();
        DoctorProfile doctor = appointment.getDoctor();
        PatientProfile patient = appointment.getPatient();
        Prescription prescription = consultation.getPrescription();
        boolean includeClinicalDetail = !isPatientView || consultation.getStatus() == ConsultationStatus.COMPLETED;
        boolean includePrescription = !isPatientView
                || (prescription != null && prescription.getStatus() == PrescriptionStatus.FINALIZED);

        return ConsultationResponse.builder()
                .id(consultation.getId())
                .appointmentId(appointment.getId())
                .doctorId(doctor.getId())
                .doctorName(fullName(doctor.getFirstName(), doctor.getSurname1(), doctor.getSurname2()))
                .doctorRut(doctor.getRut())
                .doctorSpecialization(doctor.getSpecialization())
                .patientId(patient.getId())
                .patientName(appointment.getRecipientFirstName() == null ? fullName(patient.getFirstName(), patient.getSurname1(), patient.getSurname2()) : fullName(appointment.getRecipientFirstName(), appointment.getRecipientSurname1(), appointment.getRecipientSurname2()))
                .patientRut(appointment.getRecipientRut() == null ? patient.getRut() : appointment.getRecipientRut())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .appointmentStatus(appointment.getStatus())
                .status(consultation.getStatus())
                .chiefComplaint(includeClinicalDetail ? consultation.getChiefComplaint() : null)
                .diagnosis(includeClinicalDetail ? consultation.getDiagnosis() : null)
                .notes(includeClinicalDetail ? consultation.getNotes() : null)
                .medicalHistory(includeClinicalDetail ? consultation.getMedicalHistory() : null)
                .surgicalHistory(includeClinicalDetail ? consultation.getSurgicalHistory() : null)
                .psychiatricHistory(includeClinicalDetail ? consultation.getPsychiatricHistory() : null)
                .familyHistory(includeClinicalDetail ? consultation.getFamilyHistory() : null)
                .tobaccoUse(includeClinicalDetail ? consultation.getTobaccoUse() : null)
                .tobaccoDetails(includeClinicalDetail ? consultation.getTobaccoDetails() : null)
                .alcoholUse(includeClinicalDetail ? consultation.getAlcoholUse() : null)
                .alcoholDetails(includeClinicalDetail ? consultation.getAlcoholDetails() : null)
                .drugUse(includeClinicalDetail ? consultation.getDrugUse() : null)
                .drugDetails(includeClinicalDetail ? consultation.getDrugDetails() : null)
                .anamnesis(includeClinicalDetail ? consultation.getAnamnesis() : null)
                .mentalExam(includeClinicalDetail ? consultation.getMentalExam() : null)
                .cie10Diagnosis(includeClinicalDetail ? consultation.getCie10Diagnosis() : null)
                .indications(includeClinicalDetail ? consultation.getIndications() : null)
                .certificateReason(includeClinicalDetail ? consultation.getCertificateReason() : null)
                .prescriptionStatus(includePrescription && prescription != null ? prescription.getStatus() : null)
                .prescriptionType(includePrescription && prescription != null ? prescription.getType() : null)
                .createdAt(consultation.getCreatedAt())
                .finalizedAt(consultation.getFinalizedAt())
                .build();
    }

    private String fullName(String firstName, String surname1, String surname2) {
        return String.join(" ", firstName, surname1, surname2).trim();
    }
}
