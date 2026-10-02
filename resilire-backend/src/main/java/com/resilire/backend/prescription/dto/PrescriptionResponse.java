package com.resilire.backend.prescription.dto;

import com.resilire.backend.prescription.PrescriptionStatus;
import com.resilire.backend.prescription.PrescriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long consultationId;
    private PrescriptionType type;
    private PrescriptionStatus status;
    private String notes;
    private List<MedicineItem> medicines;
    private List<TestItem> tests;
    private String fileName;
    private String fileContentType;
    private Long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime finalizedAt;
}
