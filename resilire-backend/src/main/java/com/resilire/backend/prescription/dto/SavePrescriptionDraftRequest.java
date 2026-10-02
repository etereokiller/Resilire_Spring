package com.resilire.backend.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SavePrescriptionDraftRequest {

    @Size(max = 2000, message = "validation.notes.tooLong")
    private String notes;

    @Valid
    private List<MedicineItem> medicines = new ArrayList<>();

    @Valid
    private List<TestItem> tests = new ArrayList<>();
}
