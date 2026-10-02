package com.resilire.backend.consultation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilire.backend.appointment.Appointment;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates a Google Meet-enabled Calendar event for an appointment, using a Workspace
 * service account with domain-wide delegation (impersonating a real calendar owner via
 * the "sub" claim - a bare service account calendar cannot host Meet conferences).
 *
 * This is the production path - it requires Google Workspace, since domain-wide
 * delegation is not available on personal Gmail accounts. For dev/testing against a
 * personal Gmail account, see {@link GoogleOAuthMeetLinkProvider} instead.
 *
 * Only active when google.calendar.provider=service-account; see application.yml for
 * the required GOOGLE_* environment variables.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "google.calendar.provider", havingValue = "service-account")
public class GoogleMeetLinkProvider implements MeetingLinkProvider {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String EVENTS_URL =
            "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.events";

    private final String serviceAccountEmail;
    private final String impersonateUser;
    private final String timezone;
    private final PrivateKey privateKey;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleMeetLinkProvider(
            @Value("${google.calendar.service-account-email}") String serviceAccountEmail,
            @Value("${google.calendar.private-key}") String privateKeyPem,
            @Value("${google.calendar.impersonate-user}") String impersonateUser,
            @Value("${google.calendar.timezone:UTC}") String timezone) {
        this.serviceAccountEmail = serviceAccountEmail;
        this.impersonateUser = impersonateUser;
        this.timezone = timezone;
        this.privateKey = parsePrivateKey(privateKeyPem);
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

        return new MeetingDetails(hangoutLink, "GOOGLE_MEET", eventId);
    }

    private String fetchAccessToken() {
        String assertion = buildSignedAssertion();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        form.add("assertion", assertion);

        JsonNode response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("access_token")) {
            throw new IllegalStateException("Failed to obtain a Google OAuth access token");
        }
        return response.get("access_token").asText();
    }

    private String buildSignedAssertion() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofHours(1).toMillis());

        return Jwts.builder()
                .issuer(serviceAccountEmail)
                .subject(impersonateUser)
                .claim("scope", CALENDAR_SCOPE)
                .claim("aud", TOKEN_URL)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private static PrivateKey parsePrivateKey(String pem) {
        try {
            String normalized = pem.replace("\\n", "\n")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(normalized);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Invalid google.calendar.private-key - expected a PKCS8 PEM-encoded RSA private key", e);
        }
    }
}
