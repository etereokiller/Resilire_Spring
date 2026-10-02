package com.resilire.backend.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class PatientProfileResponse {
    private Long id;
    private String email;
    private String rut;
    private String firstName;
    private String surname1;
    private String surname2;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
}
