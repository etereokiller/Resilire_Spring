import apiClient from "./apiClient";

export const getPrescriptionAsDoctor = (consultationId) =>
  apiClient.get(`/doctors/consultations/${consultationId}/prescription`).then((res) => res.data);

export const savePrescriptionDraft = (consultationId, payload) =>
  apiClient
    .put(`/doctors/consultations/${consultationId}/prescription`, payload)
    .then((res) => res.data);

export const finalizePrescription = (consultationId) =>
  apiClient
    .post(`/doctors/consultations/${consultationId}/prescription/finalize`)
    .then((res) => res.data);

export const downloadPrescriptionAsDoctor = (consultationId) =>
  apiClient
    .get(`/doctors/consultations/${consultationId}/prescription/download`, { responseType: "blob" })
    .then((res) => res.data);

export const downloadMedicalCertificateAsDoctor = (consultationId) =>
  apiClient
    .get(`/doctors/consultations/${consultationId}/prescription/certificate/download`, { responseType: "blob" })
    .then((res) => res.data);

export const getPrescriptionAsPatient = (consultationId) =>
  apiClient.get(`/patients/consultations/${consultationId}/prescription`).then((res) => res.data);

export const downloadPrescriptionAsPatient = (consultationId) =>
  apiClient
    .get(`/patients/consultations/${consultationId}/prescription/download`, { responseType: "blob" })
    .then((res) => res.data);

export const uploadOfflinePrescription = (appointmentId, file, notes) => {
  const formData = new FormData();
  formData.append("file", file);
  if (notes) {
    formData.append("notes", notes);
  }
  return apiClient
    .post(`/patients/appointments/${appointmentId}/prescription/upload`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    })
    .then((res) => res.data);
};
