package com.resilire.backend.appointment;

import com.resilire.backend.appointment.dto.AppointmentResponse;
import com.resilire.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(appointmentService.listForDoctor(principal.getId()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAsDoctor(principal.getId(), id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAsDoctor(principal.getId(), id));
    }
}
