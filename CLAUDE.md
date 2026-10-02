# Resilire — release-1

Two independent projects tracking the Application Scope Agreement's release plan:

- `resilire-backend/` — Spring Boot 3 (Java 17), PostgreSQL, Flyway, JWT auth (API on :8081)
- `resilire-frontend/` — React (Vite), React Router, Axios (app on :5173)

See root `README.md` for release status, quick start, and localization details.

## Online consultation / Google Meet integration (Release 4)

Every booked appointment gets a meeting link via `MeetingLinkProvider`
(`resilire-backend/src/main/java/com/resilire/backend/consultation/`), which has three
implementations selected by the `google.calendar.provider` property:

- `PlaceholderMeetingLinkProvider.java` — active by default (`google.calendar.provider=placeholder`).
  Generates `https://meet.resilire.local/<uuid>` with no external calls. This is the
  `meet.resilire.local` link seen in local/dev environments.
- `GoogleMeetLinkProvider.java` — active when `google.calendar.provider=service-account`.
  **Production.** Creates a real Google Calendar event with `conferenceData` (Hangouts Meet)
  via a Workspace **service account with domain-wide delegation** (impersonates a real
  calendar-owning mailbox using the JWT `sub` claim, since a bare service account calendar
  can't host Meet conferences). Requires Google Workspace.
- `GoogleOAuthMeetLinkProvider.java` — active when `google.calendar.provider=oauth`.
  **Dev/testing.** Same Calendar-events call, but authenticates via a standard OAuth2
  refresh-token grant against a **personal Gmail account's own calendar** — no Workspace
  or domain-wide delegation needed.

Config lives in `resilire-backend/src/main/resources/application.yml` under `google.calendar.*`,
driven by env vars:

| Env var | Purpose | Used by |
|---|---|---|
| `GOOGLE_CALENDAR_PROVIDER` | `placeholder` (default) / `service-account` / `oauth` | all |
| `GOOGLE_CALENDAR_TIMEZONE` | Defaults to `UTC` | service-account, oauth |
| `GOOGLE_SERVICE_ACCOUNT_EMAIL` | Service account email (`...@...iam.gserviceaccount.com`) | service-account |
| `GOOGLE_SERVICE_ACCOUNT_PRIVATE_KEY` | PEM private key from the service account's JSON key | service-account |
| `GOOGLE_CALENDAR_IMPERSONATE_USER` | Real Workspace mailbox to impersonate | service-account |
| `GOOGLE_OAUTH_CLIENT_ID` | OAuth client ID (Desktop app type) | oauth |
| `GOOGLE_OAUTH_CLIENT_SECRET` | OAuth client secret | oauth |
| `GOOGLE_OAUTH_REFRESH_TOKEN` | Refresh token obtained once via the personal Gmail consent screen | oauth |

### Steps to go live with real Google Meet links (production — service account)

Requires **Google Workspace** (domain-wide delegation is not available on free/personal
Gmail accounts).

1. In Google Cloud Console, create/select a project, enable the **Google Calendar API**.
2. Create a **service account** (IAM & Admin → Service Accounts), then generate a JSON key
   for it. Note the `client_email` and `private_key` fields.
3. In Workspace Admin Console (admin.google.com) → Security → API Controls →
   **Domain-wide Delegation** → add a new client using the service account's **OAuth2 Client
   ID** (not the email), with scope `https://www.googleapis.com/auth/calendar.events`.
4. Pick a real mailbox in the domain to impersonate (e.g. `scheduling@yourdomain.com`) —
   this account's calendar is what events/Meet links get created on.
5. Set `GOOGLE_CALENDAR_PROVIDER=service-account`, `GOOGLE_SERVICE_ACCOUNT_EMAIL`,
   `GOOGLE_SERVICE_ACCOUNT_PRIVATE_KEY` (from the JSON key), `GOOGLE_CALENDAR_IMPERSONATE_USER`
   before starting `resilire-backend`.
6. Restart the backend — `GoogleMeetLinkProvider` becomes the active bean
   (`@ConditionalOnProperty` swap), and newly booked appointments get real
   `meet.google.com/...` links instead of `meet.resilire.local/...`.

### Steps for dev/testing with a personal Gmail account (OAuth)

No Workspace needed — events get created on the personal Gmail account's own calendar.

1. In Google Cloud Console, create/select a project, enable the **Google Calendar API**.
2. Configure the **OAuth consent screen** (External, Testing mode is fine) and add the
   personal Gmail address as a test user.
3. Create an **OAuth client ID** (APIs & Services → Credentials) of type **Web application**
   (not Desktop app — Desktop clients don't allow a custom redirect URI, which the Playground
   needs and Desktop-type clients will fail with `redirect_uri_mismatch`). Add authorized
   redirect URI `https://developers.google.com/oauthplayground`. Note the client ID and secret.
4. One-time only: obtain a refresh token by completing the consent flow as that Gmail
   account with scope `https://www.googleapis.com/auth/calendar.events` — e.g. via
   [Google's OAuth 2.0 Playground](https://developers.google.com/oauthplayground)
   (gear icon → "Use your own OAuth credentials" → paste the client ID/secret → authorize
   the Calendar scope → exchange for tokens) and copy the resulting `refresh_token`.
5. Set `GOOGLE_CALENDAR_PROVIDER=oauth`, `GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET`,
   `GOOGLE_OAUTH_REFRESH_TOKEN` before starting `resilire-backend`.
6. Restart the backend — `GoogleOAuthMeetLinkProvider` becomes the active bean, and newly
   booked appointments get real `meet.google.com/...` links created on that personal
   Gmail account's calendar.

The refresh token doesn't expire from normal use (only if revoked, unused for 6 months,
or the consent screen is in Testing mode past its 7-day test-user token expiry — if tokens
start failing, just repeat step 4).

No frontend changes needed — `AppointmentResponse.java` / the appointments pages just render
whatever link the active provider returns.
