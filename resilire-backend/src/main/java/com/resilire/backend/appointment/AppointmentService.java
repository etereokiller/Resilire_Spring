package com.resilire.backend.appointment;

import com.resilire.backend.appointment.dto.AppointmentResponse;
import com.resilire.backend.appointment.dto.BookAppointmentRequest;
import com.resilire.backend.common.exception.DuplicateResourceException;
import com.resilire.backend.common.exception.ResourceNotFoundException;
import com.resilire.backend.consultation.MeetingDetails;
import com.resilire.backend.consultation.MeetingLinkProvider;
import com.resilire.backend.doctor.DoctorAvailability;
import com.resilire.backend.doctor.DoctorAvailabilityRepository;
import com.resilire.backend.doctor.DoctorProfile;
import com.resilire.backend.doctor.DoctorProfileRepository;
import com.resilire.backend.patient.PatientProfile;
import com.resilire.backend.patient.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final MeetingLinkProvider meetingLinkProvider;

    @Value("${consultation.join-window-minutes-before:15}")
    private int joinWindowMinutesBefore;

    @Transactional
    public AppointmentResponse bookAppointment(Long patientUserId, BookAppointmentRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("error.appointment.endAfterStart");
        }

        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));

        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .filter(d -> d.getUser().isEnabled())
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.notFound"));

        DayOfWeek requestedDay = request.getAppointmentDate().getDayOfWeek();
        List<DoctorAvailability> daySlots =
                doctorAvailabilityRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), requestedDay);

        boolean withinAvailability = daySlots.stream().anyMatch(slot ->
                !request.getStartTime().isBefore(slot.getStartTime())
                        && !request.getEndTime().isAfter(slot.getEndTime())
                        && java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes()
                           == slot.getSlotDurationMinutes()
                        && java.time.Duration.between(slot.getStartTime(), request.getStartTime()).toMinutes()
                           % slot.getSlotDurationMinutes() == 0);
        if (!withinAvailability) {
            throw new IllegalArgumentException("error.appointment.outsideAvailability");
        }

        List<Appointment> sameDayAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatus(
                        doctor.getId(), request.getAppointmentDate(), AppointmentStatus.SCHEDULED);
        boolean overlaps = sameDayAppointments.stream().anyMatch(existing ->
                existing.getStartTime().isBefore(request.getEndTime())
                        && request.getStartTime().isBefore(existing.getEndTime()));
        if (overlaps) {
            throw new DuplicateResourceException("error.appointment.slotTaken");
        }

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .patientComments(request.getPatientComments())
                .paymentMethod(request.getPaymentMethod())
                .recipientRut(blankToNull(request.getRecipientRut()))
                .recipientFirstName(blankToNull(request.getRecipientFirstName()))
                .recipientSurname1(blankToNull(request.getRecipientSurname1()))
                .recipientSurname2(blankToNull(request.getRecipientSurname2()))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointment = appointmentRepository.save(appointment);
        attachMeeting(appointment);
        return toResponse(appointment);
    }

    /**
     * Creates the online-consultation meeting for a freshly booked appointment.
     * A provider failure (e.g. Google API outage) must not block the booking itself -
     * it is logged and the appointment is left without a meeting link for now.
     */
    private void attachMeeting(Appointment appointment) {
        try {
            MeetingDetails meeting = meetingLinkProvider.createMeeting(appointment);
            appointment.setMeetingLink(meeting.link());
            appointment.setMeetingProvider(meeting.provider());
            appointment.setMeetingEventId(meeting.externalEventId());
            appointmentRepository.save(appointment);
        } catch (Exception e) {
            log.error("Failed to create consultation meeting for appointment {}", appointment.getId(), e);
        }
    }

    private void ensureMeetingLink(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED && appointment.getMeetingLink() == null) {
            attachMeeting(appointment);
        }
    }

    @Transactional
    public List<AppointmentResponse> listForPatient(Long patientUserId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId())
                .stream().peek(this::ensureMeetingLink).map(this::toResponse).toList();
    }

    @Transactional
    public List<AppointmentResponse> listForDoctor(Long doctorUserId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctor.getId())
                .stream().peek(this::ensureMeetingLink).map(this::toResponse).toList();
    }

    @Transactional
    public AppointmentResponse cancelAsPatient(Long patientUserId, Long appointmentId) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.patient.profileNotFound"));
        Appointment appointment = findById(appointmentId);
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("error.appointment.forbidden");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAsDoctor(Long doctorUserId, Long appointmentId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));
        Appointment appointment = findById(appointmentId);
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("error.appointment.forbidden");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAsDoctor(Long doctorUserId, Long appointmentId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.doctor.profileNotFound"));
        Appointment appointment = findById(appointmentId);
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("error.appointment.forbidden");
        }
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("error.appointment.onlyScheduledCanComplete");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(appointmentRepository.save(appointment));
    }

    private Appointment findById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("error.appointment.notFound"));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        DoctorProfile doctor = appointment.getDoctor();
        PatientProfile patient = appointment.getPatient();
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .doctorId(doctor.getId())
                .doctorName(fullName(doctor.getFirstName(), doctor.getSurname1(), doctor.getSurname2()))
                .doctorRut(doctor.getRut())
                .doctorSpecialization(doctor.getSpecialization())
                .patientId(patient.getId())
                .patientName(recipientName(appointment, patient))
                .patientRut(appointment.getRecipientRut() != null ? appointment.getRecipientRut() : patient.getRut())
                .recipientFirstName(appointment.getRecipientFirstName())
                .recipientSurname1(appointment.getRecipientSurname1())
                .recipientSurname2(appointment.getRecipientSurname2())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .patientComments(appointment.getPatientComments())
                .paymentMethod(appointment.getPaymentMethod())
                .paymentStatus(appointment.getPaymentStatus())
                .status(appointment.getStatus())
                .meetingLink(appointment.getMeetingLink())
                .joinable(isJoinable(appointment))
                .consultationId(appointment.getConsultation() != null ? appointment.getConsultation().getId() : null)
                .build();
    }

    /**
     * The consultation link is only actionable a short window before the appointment
     * starts. Once opened, the link remains available so a clinician can recover from
     * a late start without losing access to the meeting room.
     */
    private boolean isJoinable(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED || appointment.getMeetingLink() == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime joinsFrom = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime())
                .minusMinutes(joinWindowMinutesBefore);
        return !now.isBefore(joinsFrom);
    }

    private String fullName(String firstName, String surname1, String surname2) {
        return String.join(" ", firstName, surname1, surname2).trim();
    }

    private String recipientName(Appointment appointment, PatientProfile accountHolder) {
        return appointment.getRecipientFirstName() == null ? fullName(accountHolder.getFirstName(), accountHolder.getSurname1(), accountHolder.getSurname2()) : fullName(appointment.getRecipientFirstName(), appointment.getRecipientSurname1(), appointment.getRecipientSurname2());
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
