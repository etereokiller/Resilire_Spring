ALTER TABLE consultations
    ADD COLUMN medical_history VARCHAR(2000),
    ADD COLUMN surgical_history VARCHAR(2000),
    ADD COLUMN psychiatric_history VARCHAR(2000),
    ADD COLUMN family_history VARCHAR(2000),
    ADD COLUMN tobacco_use BOOLEAN,
    ADD COLUMN alcohol_use BOOLEAN,
    ADD COLUMN alcohol_details VARCHAR(1000),
    ADD COLUMN drug_use BOOLEAN,
    ADD COLUMN anamnesis VARCHAR(4000),
    ADD COLUMN mental_exam VARCHAR(4000),
    ADD COLUMN cie10_diagnosis VARCHAR(500),
    ADD COLUMN indications VARCHAR(4000),
    ADD COLUMN certificate_reason VARCHAR(2000);
