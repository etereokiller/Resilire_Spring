import apiClient from "./apiClient";

export const openConsultation = (appointmentId) =>
  apiClient.post("/doctors/consultations/open", { appointmentId }).then((res) => res.data);

export const listMyConsultationsAsDoctor = () =>
  apiClient.get("/doctors/consultations").then((res) => res.data);

export const getConsultationAsDoctor = (id) =>
  apiClient.get(`/doctors/consultations/${id}`).then((res) => res.data);

export const updateConsultationDetails = (id, payload) =>
  apiClient.put(`/doctors/consultations/${id}/details`, payload).then((res) => res.data);

export const listMyConsultationsAsPatient = () =>
  apiClient.get("/patients/consultations").then((res) => res.data);

export const getConsultationAsPatient = (id) =>
  apiClient.get(`/patients/consultations/${id}`).then((res) => res.data);
