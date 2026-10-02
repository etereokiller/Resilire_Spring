CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor_profiles(id) ON DELETE CASCADE,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);

-- Prevents two scheduled appointments from starting at the exact same slot for a doctor.
-- Overlapping-but-not-identical slots are additionally guarded in the service layer.
CREATE UNIQUE INDEX uq_appointments_doctor_slot
    ON appointments(doctor_id, appointment_date, start_time)
    WHERE status = 'SCHEDULED';
