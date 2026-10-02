ALTER TABLE appointments
    ADD COLUMN meeting_link VARCHAR(500),
    ADD COLUMN meeting_provider VARCHAR(20),
    ADD COLUMN meeting_event_id VARCHAR(255),
    ADD COLUMN reminder_sent BOOLEAN NOT NULL DEFAULT FALSE;
