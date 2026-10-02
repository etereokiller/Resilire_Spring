package com.resilire.backend.consultation;

import com.resilire.backend.consultation.dto.ConsultationResponse;
import com.resilire.backend.consultation.dto.OpenConsultationRequest;
import com.resilire.backend.consultation.dto.UpdateConsultationDetailsRequest;
import com.resilire.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors/consultations")
@RequiredArgsConstructor
public class DoctorConsultationController {

    private final ConsultationService consultationService;

    @PostMapping("/open")
    public ResponseEntity<ConsultationResponse> open(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody OpenConsultationRequest request) {
        return ResponseEntity.ok(
                consultationService.openConsultation(principal.getId(), request.getAppointmentId()));
    }

    @GetMapping
    public ResponseEntity<List<ConsultationResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(consultationService.listForDoctor(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> get(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return ResponseEntity.ok(consultationService.getForDoctor(principal.getId(), id));
    }

    @PutMapping("/{id}/details")
    public ResponseEntity<ConsultationResponse> updateDetails(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateConsultationDetailsRequest request) {
        return ResponseEntity.ok(consultationService.updateDetails(principal.getId(), id, request));
    }
}
