package com.resilire.backend.appointment.dto;

import com.resilire.backend.appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorRut;
    private String doctorSpecialization;
    private Long patientId;
    private String patientName;
    private String patientRut;
    private String recipientFirstName;
    private String recipientSurname1;
    private String recipientSurname2;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String patientComments;
    private String paymentMethod;
    private String paymentStatus;
    private AppointmentStatus status;
    private String meetingLink;
    private boolean joinable;
    private Long consultationId;
}
