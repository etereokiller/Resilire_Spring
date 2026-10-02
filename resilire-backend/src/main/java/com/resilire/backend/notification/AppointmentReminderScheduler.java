package com.resilire.backend.notification;

import com.resilire.backend.appointment.Appointment;
import com.resilire.backend.appointment.AppointmentRepository;
import com.resilire.backend.appointment.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * Polls for scheduled appointments starting within the configured reminder window
 * and sends a one-time reminder email per appointment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentReminderService reminderService;

    @Value("${notification.reminder.minutes-before:60}")
    private int reminderMinutesBefore;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Appointment> candidates = Stream.concat(
                appointmentRepository.findByStatusAndReminderSentFalseAndAppointmentDate(
                        AppointmentStatus.SCHEDULED, today).stream(),
                appointmentRepository.findByStatusAndReminderSentFalseAndAppointmentDate(
                        AppointmentStatus.SCHEDULED, today.plusDays(1)).stream())
                .toList();

        for (Appointment appointment : candidates) {
            LocalDateTime start = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime());
            long minutesUntilStart = now.until(start, java.time.temporal.ChronoUnit.MINUTES);
            if (minutesUntilStart <= reminderMinutesBefore && minutesUntilStart >= 0) {
                reminderService.sendReminder(appointment);
                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);
            }
        }
    }
}
