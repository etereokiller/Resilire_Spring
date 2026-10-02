package com.resilire.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
}
