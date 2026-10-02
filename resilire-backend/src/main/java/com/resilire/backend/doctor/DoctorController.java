package com.resilire.backend.doctor;

import com.resilire.backend.doctor.dto.AvailabilitySlotRequest;
import com.resilire.backend.doctor.dto.AvailabilitySlotResponse;
import com.resilire.backend.doctor.dto.DoctorProfileResponse;
import com.resilire.backend.doctor.dto.DoctorProfileUpdateRequest;
import com.resilire.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/me")
    public ResponseEntity<DoctorProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(doctorService.getProfileByUserId(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<DoctorProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DoctorProfileUpdateRequest request) {
        return ResponseEntity.ok(doctorService.updateProfile(principal.getId(), request));
    }

    @PostMapping(value = "/me/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DoctorProfileResponse> uploadProfilePhoto(@AuthenticationPrincipal CustomUserDetails principal, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(doctorService.updateProfilePhoto(principal.getId(), file));
    }

    @GetMapping("/me/availability")
    public ResponseEntity<List<AvailabilitySlotResponse>> listMyAvailability(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(doctorService.listAvailability(principal.getId()));
    }

    @PostMapping("/me/availability")
    public ResponseEntity<AvailabilitySlotResponse> addAvailability(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody AvailabilitySlotRequest request) {
        return ResponseEntity.ok(doctorService.addAvailability(principal.getId(), request));
    }

    @DeleteMapping("/me/availability/{id}")
    public ResponseEntity<Void> deleteAvailability(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        doctorService.deleteAvailability(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
