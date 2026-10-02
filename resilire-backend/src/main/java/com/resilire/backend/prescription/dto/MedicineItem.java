package com.resilire.backend.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineItem {

    @NotBlank(message = "validation.medicineName.required")
    @Size(max = 200, message = "validation.medicineName.tooLong")
    private String medicineName;

    @Size(max = 100, message = "validation.dosage.tooLong")
    private String dosage;

    @Size(max = 100, message = "validation.frequency.tooLong")
    private String frequency;

    @Size(max = 100, message = "validation.duration.tooLong")
    private String duration;

    @Size(max = 500, message = "validation.instructions.tooLong")
    private String instructions;
}
