package com.resilire.backend.prescription;

import com.resilire.backend.prescription.dto.DownloadableFile;
import com.resilire.backend.prescription.dto.PrescriptionResponse;
import com.resilire.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientPrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/consultations/{consultationId}/prescription")
    public ResponseEntity<PrescriptionResponse> get(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long consultationId) {
        return ResponseEntity.ok(prescriptionService.getForPatient(principal.getId(), consultationId));
    }

    @GetMapping("/consultations/{consultationId}/prescription/download")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long consultationId) {
        DownloadableFile file = prescriptionService.downloadForPatient(principal.getId(), consultationId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }

    @PostMapping(
            value = "/appointments/{appointmentId}/prescription/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrescriptionResponse> uploadOffline(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long appointmentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes) {
        return ResponseEntity.ok(prescriptionService.uploadOffline(principal.getId(), appointmentId, file, notes));
    }
}
