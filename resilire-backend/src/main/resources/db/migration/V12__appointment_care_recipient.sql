ALTER TABLE appointments
    ADD COLUMN recipient_rut VARCHAR(12),
    ADD COLUMN recipient_first_name VARCHAR(100),
    ADD COLUMN recipient_surname1 VARCHAR(100),
    ADD COLUMN recipient_surname2 VARCHAR(100);
