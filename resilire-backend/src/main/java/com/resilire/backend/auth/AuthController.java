package com.resilire.backend.auth;

import com.resilire.backend.auth.dto.*;
import com.resilire.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/patient")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<AuthResponse> registerDoctor(@Valid @RequestBody RegisterDoctorRequest request) {
        return ResponseEntity.ok(authService.registerDoctor(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password-recovery")
    public ResponseEntity<Void> requestPasswordRecovery(@RequestBody Map<String, String> request) {
        authService.requestPasswordRecovery(request.get("email"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal));
    }
}
