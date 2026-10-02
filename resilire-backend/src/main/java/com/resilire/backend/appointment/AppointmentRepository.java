package com.resilire.backend.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(
            Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

    List<Appointment> findByStatusAndReminderSentFalseAndAppointmentDate(
            AppointmentStatus status, LocalDate appointmentDate);
}
