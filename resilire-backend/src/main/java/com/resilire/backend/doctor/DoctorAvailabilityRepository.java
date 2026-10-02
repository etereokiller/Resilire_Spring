package com.resilire.backend.doctor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);

    boolean existsByIdAndDoctorId(Long id, Long doctorId);
}
