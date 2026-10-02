package com.resilire.backend.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientProfileUpdateRequest {

    @NotBlank(message = "validation.firstName.required")
    private String firstName;

    @NotBlank(message = "validation.surname1.required")
    private String surname1;

    @NotBlank(message = "validation.surname2.required")
    private String surname2;

    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}
