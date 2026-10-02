package com.resilire.backend.consultation;

import com.resilire.backend.consultation.dto.ConsultationResponse;
import com.resilire.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients/consultations")
@RequiredArgsConstructor
public class PatientConsultationController {

    private final ConsultationService consultationService;

    @GetMapping
    public ResponseEntity<List<ConsultationResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(consultationService.listForPatient(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> get(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return ResponseEntity.ok(consultationService.getForPatient(principal.getId(), id));
    }
}
