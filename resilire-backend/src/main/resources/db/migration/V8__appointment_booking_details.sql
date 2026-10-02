ALTER TABLE appointments ADD COLUMN patient_comments VARCHAR(1000);
ALTER TABLE appointments ADD COLUMN payment_method VARCHAR(30);
ALTER TABLE appointments ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
