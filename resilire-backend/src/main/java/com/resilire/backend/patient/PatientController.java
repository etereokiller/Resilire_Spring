package com.resilire.backend.patient;

import com.resilire.backend.patient.dto.PatientProfileResponse;
import com.resilire.backend.patient.dto.PatientProfileUpdateRequest;
import com.resilire.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/me")
    public ResponseEntity<PatientProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(patientService.getProfileByUserId(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<PatientProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PatientProfileUpdateRequest request) {
        return ResponseEntity.ok(patientService.updateProfile(principal.getId(), request));
    }
}
