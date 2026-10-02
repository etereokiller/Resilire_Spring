package com.resilire.backend.consultation;

import com.resilire.backend.appointment.Appointment;
import com.resilire.backend.prescription.Prescription;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The digital consultation record for a booked appointment - Release 5 scope.
 * Doctor-authored fields (chiefComplaint/diagnosis/notes) are only editable while
 * status is OPEN; finalizing the associated {@link com.resilire.backend.prescription.Prescription}
 * locks this record to COMPLETED.
 */
@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 4000)
    private String notes;

    @Column(name = "medical_history", length = 2000)
    private String medicalHistory;
    @Column(name = "surgical_history", length = 2000)
    private String surgicalHistory;
    @Column(name = "psychiatric_history", length = 2000)
    private String psychiatricHistory;
    @Column(name = "family_history", length = 2000)
    private String familyHistory;
    @Column(name = "tobacco_use")
    private Boolean tobaccoUse;
    @Column(name = "tobacco_details", length = 1000)
    private String tobaccoDetails;
    @Column(name = "alcohol_use")
    private Boolean alcoholUse;
    @Column(name = "alcohol_details", length = 1000)
    private String alcoholDetails;
    @Column(name = "drug_use")
    private Boolean drugUse;
    @Column(name = "drug_details", length = 1000)
    private String drugDetails;
    @Column(name = "anamnesis", length = 4000)
    private String anamnesis;
    @Column(name = "mental_exam", length = 4000)
    private String mentalExam;
    @Column(name = "cie10_diagnosis", length = 500)
    private String cie10Diagnosis;
    @Column(name = "indications", length = 4000)
    private String indications;
    @Column(name = "certificate_reason", length = 2000)
    private String certificateReason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationStatus status = ConsultationStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @OneToOne(mappedBy = "consultation", fetch = FetchType.LAZY)
    private Prescription prescription;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
