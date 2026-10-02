package com.resilire.backend.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class BookableSlotResponse {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
