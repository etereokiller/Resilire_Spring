CREATE TABLE consultations (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
    chief_complaint VARCHAR(1000),
    diagnosis VARCHAR(1000),
    notes VARCHAR(4000),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    finalized_at TIMESTAMP
);

CREATE INDEX idx_consultations_appointment_id ON consultations(appointment_id);

CREATE TABLE prescriptions (
    id BIGSERIAL PRIMARY KEY,
    consultation_id BIGINT NOT NULL UNIQUE REFERENCES consultations(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('GENERATED', 'OFFLINE_UPLOAD')),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'FINALIZED')),
    notes VARCHAR(2000),
    file_name VARCHAR(255),
    file_content_type VARCHAR(100),
    file_size BIGINT,
    file_storage_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    finalized_at TIMESTAMP
);

CREATE INDEX idx_prescriptions_consultation_id ON prescriptions(consultation_id);

CREATE TABLE prescription_medicines (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    instructions VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_prescription_medicines_prescription_id ON prescription_medicines(prescription_id);

CREATE TABLE prescription_tests (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    test_name VARCHAR(200) NOT NULL,
    instructions VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_prescription_tests_prescription_id ON prescription_tests(prescription_id);
