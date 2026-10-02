-- Release 6 - Chilean RUT (national ID) on doctor and patient profiles.
-- Backfill with a distinct placeholder before enforcing NOT NULL/UNIQUE so this migration is
-- safe to run even against a dev database that already has profile rows.
ALTER TABLE doctor_profiles ADD COLUMN rut VARCHAR(12);
UPDATE doctor_profiles SET rut = 'PENDING-' || id WHERE rut IS NULL;
ALTER TABLE doctor_profiles ALTER COLUMN rut SET NOT NULL;
ALTER TABLE doctor_profiles ADD CONSTRAINT uk_doctor_profiles_rut UNIQUE (rut);

ALTER TABLE patient_profiles ADD COLUMN rut VARCHAR(12);
UPDATE patient_profiles SET rut = 'PENDING-' || id WHERE rut IS NULL;
ALTER TABLE patient_profiles ALTER COLUMN rut SET NOT NULL;
ALTER TABLE patient_profiles ADD CONSTRAINT uk_patient_profiles_rut UNIQUE (rut);
