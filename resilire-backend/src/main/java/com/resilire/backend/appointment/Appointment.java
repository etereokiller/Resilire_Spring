package com.resilire.backend.appointment;

import com.resilire.backend.consultation.Consultation;
import com.resilire.backend.doctor.DoctorProfile;
import com.resilire.backend.patient.PatientProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Column(name = "recipient_rut", length = 12)
    private String recipientRut;
    @Column(name = "recipient_first_name", length = 100)
    private String recipientFirstName;
    @Column(name = "recipient_surname1", length = 100)
    private String recipientSurname1;
    @Column(name = "recipient_surname2", length = 100)
    private String recipientSurname2;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "patient_comments", length = 1000)
    private String patientComments;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Builder.Default
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "PENDING";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "meeting_provider", length = 20)
    private String meetingProvider;

    @Column(name = "meeting_event_id")
    private String meetingEventId;

    @Builder.Default
    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "appointment", fetch = FetchType.LAZY)
    private Consultation consultation;

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
