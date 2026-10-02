package com.resilire.backend.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PublicDoctorDetailResponse {
    private Long id;
    private String firstName;
    private String surname1;
    private String surname2;
    private String specialization;
    private String qualification;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private String bio;
    private boolean hasProfilePhoto;
    private List<AvailabilitySlotResponse> availability;
}
