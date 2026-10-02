package com.resilire.backend.appointment;

import com.resilire.backend.appointment.dto.AppointmentResponse;
import com.resilire.backend.appointment.dto.BookAppointmentRequest;
import com.resilire.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/appointments")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> book(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(appointmentService.listForPatient(principal.getId()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAsPatient(principal.getId(), id));
    }
}
