package com.resilire.backend.consultation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenConsultationRequest {

    @NotNull(message = "validation.appointmentId.required")
    private Long appointmentId;
}
