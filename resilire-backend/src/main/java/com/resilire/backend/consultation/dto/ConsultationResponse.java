package com.resilire.backend.consultation.dto;

import com.resilire.backend.appointment.AppointmentStatus;
import com.resilire.backend.consultation.ConsultationStatus;
import com.resilire.backend.prescription.PrescriptionStatus;
import com.resilire.backend.prescription.PrescriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class ConsultationResponse {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private String doctorRut;
    private String doctorSpecialization;
    private Long patientId;
    private String patientName;
    private String patientRut;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus appointmentStatus;
    private ConsultationStatus status;
    private String chiefComplaint;
    private String diagnosis;
    private String notes;
    private String medicalHistory;
    private String surgicalHistory;
    private String psychiatricHistory;
    private String familyHistory;
    private Boolean tobaccoUse;
    private String tobaccoDetails;
    private Boolean alcoholUse;
    private String alcoholDetails;
    private Boolean drugUse;
    private String drugDetails;
    private String anamnesis;
    private String mentalExam;
    private String cie10Diagnosis;
    private String indications;
    private String certificateReason;
    private PrescriptionStatus prescriptionStatus;
    private PrescriptionType prescriptionType;
    private LocalDateTime createdAt;
    private LocalDateTime finalizedAt;
}
