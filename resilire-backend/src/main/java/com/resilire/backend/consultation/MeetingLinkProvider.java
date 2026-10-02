package com.resilire.backend.consultation;

import com.resilire.backend.appointment.Appointment;

/**
 * Creates the video-consultation meeting for a booked appointment.
 * Release 4 scope: Google Meet only, selected via google.calendar.provider:
 * {@link GoogleMeetLinkProvider} (service account, production/Workspace),
 * {@link GoogleOAuthMeetLinkProvider} (personal Gmail OAuth, dev/testing), or the
 * {@link PlaceholderMeetingLinkProvider} fallback for environments without Google credentials.
 */
public interface MeetingLinkProvider {

    MeetingDetails createMeeting(Appointment appointment);
}
