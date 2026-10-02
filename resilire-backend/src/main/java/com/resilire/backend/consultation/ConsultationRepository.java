package com.resilire.backend.consultation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByAppointmentId(Long appointmentId);

    List<Consultation> findByAppointmentDoctorIdOrderByCreatedAtDesc(Long doctorId);

    List<Consultation> findByAppointmentPatientIdOrderByCreatedAtDesc(Long patientId);
}
