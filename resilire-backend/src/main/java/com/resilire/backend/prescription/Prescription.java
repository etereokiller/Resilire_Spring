package com.resilire.backend.prescription;

import com.resilire.backend.consultation.Consultation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The prescription record for a {@link Consultation} - Release 5 scope. Either
 * doctor-authored (type GENERATED, built up as a DRAFT and locked by "Finalize
 * prescription") or a patient-supplied scanned/photographed document
 * (type OFFLINE_UPLOAD, always created directly in FINALIZED status).
 * A consultation can have at most one prescription.
 */
@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.DRAFT;

    @Column(length = 2000)
    private String notes;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_content_type", length = 100)
    private String fileContentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_storage_path", length = 500)
    private String fileStoragePath;

    @Builder.Default
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PrescriptionMedicine> medicines = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PrescriptionTest> tests = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

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
