package com.resilire.backend.notification;

import com.resilire.backend.appointment.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the "Appointment reminders" email from Release 4 of the scope agreement.
 * Falls back to logging the reminder instead of sending when SMTP is not configured
 * (notification.email.enabled=false, the default), so the scheduler can run safely
 * without real credentials.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentReminderService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:no-reply@resilire.com}")
    private String fromAddress;

    public void sendReminder(Appointment appointment) {
        String doctorEmail = appointment.getDoctor().getUser().getEmail();
        String patientEmail = appointment.getPatient().getUser().getEmail();
        String subject = "Reminder: upcoming Resilire consultation on "
                + appointment.getAppointmentDate() + " at " + appointment.getStartTime();
        String body = buildBody(appointment);

        if (!emailEnabled) {
            log.info("[reminder email - not sent, SMTP disabled] to={},{} subject=\"{}\" body=\"{}\"",
                    patientEmail, doctorEmail, subject, body);
            return;
        }

        sendTo(patientEmail, subject, body);
        sendTo(doctorEmail, subject, body);
    }

    private void sendTo(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send appointment reminder email to {}", to, e);
        }
    }

    private String buildBody(Appointment appointment) {
        String meetingLine = appointment.getMeetingLink() != null
                ? "Join here: " + appointment.getMeetingLink()
                : "Your consultation link will be shared before the appointment.";
        return "You have a Resilire consultation on " + appointment.getAppointmentDate()
                + " from " + appointment.getStartTime() + " to " + appointment.getEndTime() + ".\n"
                + meetingLine;
    }
}
