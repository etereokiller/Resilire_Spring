# Resilire

Two independent projects, tracking the Application Scope Agreement's release plan:

- `resilire-backend/` — Spring Boot 3 (Java 17), PostgreSQL, Flyway, JWT auth
- `resilire-frontend/` — React (Vite), React Router, Axios

## Quick start

1. Start PostgreSQL and create the database (see `resilire-backend/README.md`)
2. `cd resilire-backend && ./mvnw spring-boot:run` — API on :8081
3. `cd resilire-frontend && npm install && npm run dev` — app on :5173

## Release status

| Release | Scope | Status |
|---|---|---|
| 1 — User Registration & Platform Foundation | Registration, login, profiles, JWT auth, roles, admin basics | Done |
| 2 — Doctor Search & Appointment Booking | Doctor directory, availability, booking, cancel/complete | Done |
| 3 — Payment & Appointment Lifecycle | Payment gateway, invoicing | **Skipped for now** — deferred to a later pass |
| 4 — Online Consultation | Google Meet link generation, join flow, appointment reminders | Done |
| 5 — Prescription & Consultation History | Consultations, prescriptions, history | Done |
| 6 — Third-Party Leave Integration | Doctor leave sync | Not started |

Release 3 was intentionally skipped by request; Release 4 was built directly on top
of Release 2's `Appointment` entity, so payment/invoicing can be slotted in later
without reworking the consultation flow.

## What's included

- Patient registration, login, profile view/update
- Doctor registration, login, profile view/update, weekly availability management
- Doctor search/directory, appointment booking with availability + double-booking checks
- Doctors and patients can cancel appointments; doctors can mark them completed
- **Online consultation (Release 4):** every booked appointment gets a Google Meet
  link, visible to both doctor and patient; the "Join Consultation" action is only
  enabled within a configurable window before/through the appointment (default:
  15 minutes before start through the scheduled end time)
- **Appointment reminders (Release 4):** a scheduled job emails both parties once,
  a configurable number of minutes before the appointment starts (default: 60)
- **Prescription & consultation history (Release 5):** doctors open a consultation record
  against a booked appointment, add chief complaint/diagnosis/notes, build up a prescription
  (medicines with dosage/frequency/duration/instructions, plus recommended diagnostic tests)
  and finalize it, which locks the record and marks the appointment completed. Patients see
  their full consultation history, view/download the finalized prescription (rendered as a
  PDF for doctor-generated prescriptions), and can alternatively upload a manually
  prepared/offline prescription (PDF/JPEG/PNG scan or photo) associated with the appointment
  when no digital one was generated. Every prescription maintains the
  Patient → Doctor → Appointment → Consultation → Prescription association end to end.
- JWT authentication, role-based authorization (PATIENT / DOCTOR / ADMIN)
- Public landing, about, help and privacy pages
- Basic admin: list users, enable/disable accounts
- Seeded admin account: `admin@resilire.com` / `Admin@123`
- **English/Spanish localization:** every page, form, and API validation/error
  message is translated. See "Localization (English / Spanish)" below.

## Localization (English / Spanish)

The platform is built for Santiago, Chile, so Spanish is the default language, with
English available as a toggle — end to end, including backend API messages, not
just the UI.

- **Auto-detection:** on first visit, the frontend reads the browser's language
  setting (`navigator.language` / `Accept-Language`), matching `es`/`en` when
  present. If the browser reports anything else (or nothing), it defaults to
  Spanish, since Chile is the primary market. Detection logic lives in
  `resilire-frontend/src/i18n/i18n.js`.
- **Manual toggle:** the EN/ES switch in the navbar (`LanguageToggle.jsx`) overrides
  auto-detection and is remembered in `localStorage` (`resilire_language`) for
  future visits.
- **Frontend text:** every page/component uses `react-i18next`; strings live in
  `resilire-frontend/src/i18n/en.json` and `es.json`.
- **Backend messages:** the frontend sends the active language on every API call via
  the `Accept-Language` header. Spring resolves it (`LocaleConfig.java`, default
  Spanish) and validation/error messages are translated from
  `resilire-backend/src/main/resources/messages.properties` /
  `messages_es.properties` — so a Spanish-speaking user sees "El correo electrónico
  es obligatorio" instead of "Email is required", etc.
- Spanish translations use the formal "usted" register throughout, consistent with
  Chilean medical/official platforms.

### Configuring the Release 4 integrations

Both integrations run against safe local fallbacks out of the box, so nothing extra
is required for local development:

- **Google Meet** — by default (`google.calendar.provider=placeholder`) a placeholder
  meeting link (`https://meet.resilire.local/<uuid>`) is generated instead of a real
  Google Meet link. Two ways to get real Meet links, selected via `GOOGLE_CALENDAR_PROVIDER`:
  - `service-account` — **production.** Requires Google Workspace + a service account with
    domain-wide delegation (`GOOGLE_SERVICE_ACCOUNT_EMAIL`, `GOOGLE_SERVICE_ACCOUNT_PRIVATE_KEY`,
    `GOOGLE_CALENDAR_IMPERSONATE_USER`).
  - `oauth` — **dev/testing.** Works with a personal Gmail account via a standard OAuth2
    refresh-token grant (`GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET`,
    `GOOGLE_OAUTH_REFRESH_TOKEN`) — no Workspace needed.

  See `resilire-backend/src/main/resources/application.yml` and `CLAUDE.md` (root) for the
  full setup steps for each.
- **Reminder emails** — until `NOTIFICATION_EMAIL_ENABLED=true` and `SMTP_HOST` /
  `SMTP_USERNAME` / `SMTP_PASSWORD` are set, reminders are logged instead of sent.

### Release 5 — prescription storage

Patient-uploaded offline prescriptions (PDF/JPEG/PNG, 10 MB max by default) are stored on
local disk under `PRESCRIPTION_STORAGE_DIR` (default: `uploads/prescriptions`, relative to
where `resilire-backend` runs). Doctor-generated prescriptions are never written to disk —
they're rendered as a PDF on demand from the `Prescription`/medicines/tests rows at download
time. See `resilire.storage.*` in `application.yml`.

## What's explicitly excluded (per scope doc)

- Custom-built video conferencing, call recording/storage, AI consultation analysis
  (explicitly out of scope for Release 4 — Google Meet only)
- Payment, invoicing (Release 3 — deferred)
- Third-party leave integration (Release 6)
