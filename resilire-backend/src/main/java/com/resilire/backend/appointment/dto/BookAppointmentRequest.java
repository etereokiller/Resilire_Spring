package com.resilire.backend.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookAppointmentRequest {

    @NotNull(message = "validation.doctorId.required")
    private Long doctorId;

    @NotNull(message = "validation.appointmentDate.required")
    @FutureOrPresent(message = "validation.appointmentDate.futureOrPresent")
    private LocalDate appointmentDate;

    @NotNull(message = "validation.startTime.required")
    private LocalTime startTime;

    @NotNull(message = "validation.endTime.required")
    private LocalTime endTime;

    private String patientComments;
    private String paymentMethod;

    @Size(max = 12) private String recipientRut;
    @Size(max = 100) private String recipientFirstName;
    @Size(max = 100) private String recipientSurname1;
    @Size(max = 100) private String recipientSurname2;
}
