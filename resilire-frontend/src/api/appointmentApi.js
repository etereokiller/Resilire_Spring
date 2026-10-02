import apiClient from "./apiClient";

export const bookAppointment = (payload) =>
  apiClient.post("/patients/appointments", payload).then((res) => res.data);

export const listMyAppointmentsAsPatient = () =>
  apiClient.get("/patients/appointments").then((res) => res.data);

export const cancelAppointmentAsPatient = (id) =>
  apiClient.put(`/patients/appointments/${id}/cancel`).then((res) => res.data);

export const listMyAppointmentsAsDoctor = () =>
  apiClient.get("/doctors/appointments").then((res) => res.data);

export const cancelAppointmentAsDoctor = (id) =>
  apiClient.put(`/doctors/appointments/${id}/cancel`).then((res) => res.data);

export const completeAppointmentAsDoctor = (id) =>
  apiClient.put(`/doctors/appointments/${id}/complete`).then((res) => res.data);
