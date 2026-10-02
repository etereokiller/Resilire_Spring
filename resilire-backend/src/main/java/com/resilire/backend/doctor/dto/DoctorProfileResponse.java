package com.resilire.backend.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class DoctorProfileResponse {
    private Long id;
    private String email;
    private String rut;
    private String firstName;
    private String surname1;
    private String surname2;
    private String phone;
    private String specialization;
    private String qualification;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private String bio;
    private boolean hasProfilePhoto;
}
