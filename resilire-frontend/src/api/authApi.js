import apiClient from "./apiClient";

export const login = (email, password) =>
  apiClient.post("/auth/login", { email, password }).then((res) => res.data);

export const requestPasswordReset = (email) =>
  apiClient.post("/auth/password-recovery", { email }).then((res) => res.data);

export const registerPatient = (payload) =>
  apiClient.post("/auth/register/patient", payload).then((res) => res.data);

export const registerDoctor = (payload) =>
  apiClient.post("/auth/register/doctor", payload).then((res) => res.data);

export const getCurrentUser = () =>
  apiClient.get("/auth/me").then((res) => res.data);
