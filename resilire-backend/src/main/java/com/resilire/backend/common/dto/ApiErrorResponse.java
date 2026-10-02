package com.resilire.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String error;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
}
