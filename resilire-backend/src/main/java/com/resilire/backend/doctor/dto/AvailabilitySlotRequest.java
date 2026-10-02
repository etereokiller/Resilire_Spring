package com.resilire.backend.doctor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class AvailabilitySlotRequest {

    @NotNull(message = "validation.dayOfWeek.required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "validation.startTime.required")
    private LocalTime startTime;

    @NotNull(message = "validation.endTime.required")
    private LocalTime endTime;

    @NotNull(message = "validation.slotDuration.required")
    @Min(value = 5, message = "validation.slotDuration.range")
    @Max(value = 480, message = "validation.slotDuration.range")
    private Integer slotDurationMinutes;
}
