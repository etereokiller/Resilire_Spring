package com.resilire.backend.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorProfileUpdateRequest {

    @NotBlank(message = "validation.firstName.required")
    private String firstName;

    @NotBlank(message = "validation.surname1.required")
    private String surname1;

    @NotBlank(message = "validation.surname2.required")
    private String surname2;

    private String phone;
    private String specialization;
    private String qualification;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private String bio;
}
