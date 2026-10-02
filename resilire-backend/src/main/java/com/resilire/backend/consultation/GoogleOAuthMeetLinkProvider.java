package com.resilire.backend.consultation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilire.backend.appointment.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a Google Meet-enabled Calendar event for an appointment using a standard
 * OAuth2 user-consent grant (refresh token) against a personal Gmail account's own
 * calendar - no Workspace / domain-wide delegation required, so this is the dev/testing
 * counterpart to the service-account {@link GoogleMeetLinkProvider} used in production.
 *
 * The refresh token is obtained once, out of band, by completing the OAuth consent
 * screen for the personal Gmail account (see README "Dev/testing Google Meet via
 * personal Gmail (OAuth)" for the one-time steps); this provider only ever exchanges
 * that refresh token for short-lived access tokens.
 *
 * Only active when google.calendar.provider=oauth; see application.yml for the
 * required GOOGLE_OAUTH_* environment variables.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "google.calendar.provider", havingValue = "oauth")
public class GoogleOAuthMeetLinkProvider implements MeetingLinkProvider {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String timezone;
    private final RestClient restClient = RestClient.create();

    public GoogleOAuthMeetLinkProvider(
            @Value("${google.calendar.oauth.client-id}") String clientId,
            @Value("${google.calendar.oauth.client-secret}") String clientSecret,
            @Value("${google.calendar.oauth.refresh-token}") String refreshToken,
            @Value("${google.calendar.timezone:UTC}") String timezone) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.timezone = timezone;
    }

    @Override
    public MeetingDetails createMeeting(Appointment appointment) {
        String accessToken = fetchAccessToken();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime());
        LocalDateTime end = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getEndTime());

        Map<String, Object> body = new HashMap<>();
        body.put("summary", "Resilire consultation - Dr. "
                + appointment.getDoctor().getSurname1() + " " + appointment.getDoctor().getSurname2() + " / "
                + appointment.getPatient().getFirstName() + " " + appointment.getPatient().getSurname1() + " " + appointment.getPatient().getSurname2());
        body.put("start", Map.of("dateTime", start.format(fmt), "timeZone", timezone));
        body.put("end", Map.of("dateTime", end.format(fmt), "timeZone", timezone));
        body.put("attendees", List.of(
                Map.of("email", appointment.getDoctor().getUser().getEmail()),
                Map.of("email", appointment.getPatient().getUser().getEmail())));
        body.put("conferenceData", Map.of(
                "createRequest", Map.of(
                        "requestId", UUID.randomUUID().toString(),
                        "conferenceSolutionKey", Map.of("type", "hangoutsMeet"))));

        String uri = UriComponentsBuilder.fromHttpUrl(EVENTS_URL)
                .queryParam("conferenceDataVersion", 1)
                .toUriString();

        JsonNode response = restClient.post()
                .uri(uri)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        String hangoutLink = response != null && response.has("hangoutLink")
                ? response.get("hangoutLink").asText()
                : null;
        String eventId = response != null && response.has("id") ? response.get("id").asText() : null;

        if (hangoutLink == null) {
            throw new IllegalStateException("Google Calendar did not return a Meet link for the created event");
        }

        return new MeetingDetails(hangoutLink, "GOOGLE_MEET_OAUTH", eventId);
    }

    private String fetchAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        JsonNode response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("access_token")) {
            throw new IllegalStateException("Failed to refresh a Google OAuth access token - "
                    + "check google.calendar.oauth.client-id/client-secret/refresh-token");
        }
        return response.get("access_token").asText();
    }
}
