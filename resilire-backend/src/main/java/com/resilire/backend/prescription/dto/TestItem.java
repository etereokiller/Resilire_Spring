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
public class TestItem {

    @NotBlank(message = "validation.testName.required")
    @Size(max = 200, message = "validation.testName.tooLong")
    private String testName;

    @Size(max = 500, message = "validation.instructions.tooLong")
    private String instructions;
}
