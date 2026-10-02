package com.resilire.backend.doctor;

import com.resilire.backend.common.exception.ResourceNotFoundException;
import com.resilire.backend.appointment.AppointmentRepository;
import com.resilire.backend.appointment.AppointmentStatus;
import com.resilire.backend.doctor.dto.AvailabilitySlotResponse;
import com.resilire.backend.doctor.dto.BookableSlotResponse;
import com.resilire.backend.doctor.dto.PublicDoctorDetailResponse;
import com.resilire.backend.doctor.dto.PublicDoctorSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicDoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public Page<PublicDoctorSummaryResponse> search(String specialization, String name, Pageable pageable) {
        return doctorProfileRepository
                .search(blankToNull(specialization), blankToNull(name), pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public PublicDoctorDetailResponse getDoctorDetail(Long doctorId) {
        DoctorProfile doctor = doctorProfileRepository.findByIdAndUserEnabledTrue(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.notFound"));

        var availability = doctorAvailabilityRepository
                .findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctor.getId())
                .stream()
                .map(slot -> AvailabilitySlotResponse.builder()
                        .id(slot.getId())
                        .dayOfWeek(slot.getDayOfWeek())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .slotDurationMinutes(slot.getSlotDurationMinutes())
                        .build())
                .toList();

        return PublicDoctorDetailResponse.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .surname1(doctor.getSurname1())
                .surname2(doctor.getSurname2())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .bio(doctor.getBio())
                .hasProfilePhoto(doctor.getProfilePhoto() != null)
                .availability(availability)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BookableSlotResponse> getBookableSlots(Long doctorId, LocalDate date) {
        DoctorProfile doctor = doctorProfileRepository.findByIdAndUserEnabledTrue(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.notFound"));
        var booked = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatus(
                doctor.getId(), date, AppointmentStatus.SCHEDULED);
        List<BookableSlotResponse> result = new ArrayList<>();
        for (DoctorAvailability window : doctorAvailabilityRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), date.getDayOfWeek())) {
            LocalTime start = window.getStartTime();
            while (!start.plusMinutes(window.getSlotDurationMinutes()).isAfter(window.getEndTime())) {
                LocalTime end = start.plusMinutes(window.getSlotDurationMinutes());
                final LocalTime slotStart = start;
                final LocalTime slotEnd = end;
                boolean available = booked.stream().noneMatch(appointment ->
                        appointment.getStartTime().isBefore(slotEnd) && slotStart.isBefore(appointment.getEndTime()));
                result.add(BookableSlotResponse.builder().startTime(slotStart).endTime(slotEnd).available(available).build());
                start = end;
            }
        }
        return result;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private PublicDoctorSummaryResponse toSummary(DoctorProfile doctor) {
        return PublicDoctorSummaryResponse.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .surname1(doctor.getSurname1())
                .surname2(doctor.getSurname2())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .hasProfilePhoto(doctor.getProfilePhoto() != null)
                .build();
    }
}
