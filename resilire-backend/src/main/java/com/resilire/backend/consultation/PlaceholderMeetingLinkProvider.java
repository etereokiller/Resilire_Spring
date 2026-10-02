package com.resilire.backend.consultation;

import com.resilire.backend.appointment.Appointment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Local fallback used whenever Google Calendar integration is not configured
 * (google.calendar.provider=placeholder, the default). Generates a stable, unique room
 * link so the booking flow and consultation join flow can be exercised end to end
 * without any real Google credentials.
 */
@Component
@ConditionalOnProperty(name = "google.calendar.provider", havingValue = "placeholder", matchIfMissing = true)
public class PlaceholderMeetingLinkProvider implements MeetingLinkProvider {

    @Override
    public MeetingDetails createMeeting(Appointment appointment) {
        String roomCode = UUID.randomUUID().toString();
        return new MeetingDetails("https://meet.resilire.local/" + roomCode, "PLACEHOLDER", roomCode);
    }
}
