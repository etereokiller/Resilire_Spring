package com.resilire.backend.prescription;

import com.resilire.backend.prescription.dto.DownloadableFile;
import com.resilire.backend.prescription.dto.PrescriptionResponse;
import com.resilire.backend.prescription.dto.SavePrescriptionDraftRequest;
import com.resilire.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors/consultations/{consultationId}/prescription")
@RequiredArgsConstructor
public class DoctorPrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public ResponseEntity<PrescriptionResponse> get(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long consultationId) {
        return ResponseEntity.ok(prescriptionService.getOrCreateDraftForDoctor(principal.getId(), consultationId));
    }

    @PutMapping
    public ResponseEntity<PrescriptionResponse> saveDraft(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long consultationId,
            @Valid @RequestBody SavePrescriptionDraftRequest request) {
        return ResponseEntity.ok(prescriptionService.saveDraft(principal.getId(), consultationId, request));
    }

    @PostMapping("/finalize")
    public ResponseEntity<PrescriptionResponse> finalize(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long consultationId) {
        return ResponseEntity.ok(prescriptionService.finalizePrescription(principal.getId(), consultationId));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long consultationId) {
        DownloadableFile file = prescriptionService.downloadForDoctor(principal.getId(), consultationId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }

    @GetMapping("/certificate/download")
    public ResponseEntity<byte[]> downloadCertificate(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long consultationId) {
        DownloadableFile file = prescriptionService.downloadCertificateForDoctor(principal.getId(), consultationId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }
}
