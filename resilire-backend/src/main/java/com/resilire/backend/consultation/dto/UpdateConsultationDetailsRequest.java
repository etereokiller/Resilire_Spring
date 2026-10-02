package com.resilire.backend.consultation.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateConsultationDetailsRequest {

    @Size(max = 1000, message = "validation.chiefComplaint.tooLong")
    private String chiefComplaint;

    @Size(max = 1000, message = "validation.diagnosis.tooLong")
    private String diagnosis;

    @Size(max = 4000, message = "validation.notes.tooLong")
    private String notes;

    @Size(max = 2000) private String medicalHistory;
    @Size(max = 2000) private String surgicalHistory;
    @Size(max = 2000) private String psychiatricHistory;
    @Size(max = 2000) private String familyHistory;
    private Boolean tobaccoUse;
    @Size(max = 1000) private String tobaccoDetails;
    private Boolean alcoholUse;
    @Size(max = 1000) private String alcoholDetails;
    private Boolean drugUse;
    @Size(max = 1000) private String drugDetails;
    @Size(max = 4000) private String anamnesis;
    @Size(max = 4000) private String mentalExam;
    @Size(max = 500) private String cie10Diagnosis;
    @Size(max = 4000) private String indications;
    @Size(max = 2000) private String certificateReason;
}
