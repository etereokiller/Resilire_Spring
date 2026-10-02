ALTER TABLE doctor_availability
    ADD COLUMN slot_duration_minutes INTEGER NOT NULL DEFAULT 30;

ALTER TABLE doctor_availability
    ADD CONSTRAINT chk_doctor_availability_slot_duration
    CHECK (slot_duration_minutes BETWEEN 5 AND 480);
