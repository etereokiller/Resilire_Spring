import apiClient from "./apiClient";

export const getMyPatientProfile = () =>
  apiClient.get("/patients/me").then((res) => res.data);

export const updateMyPatientProfile = (payload) =>
  apiClient.put("/patients/me", payload).then((res) => res.data);
