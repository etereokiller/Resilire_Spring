import apiClient from "./apiClient";

export const getMyDoctorProfile = () =>
  apiClient.get("/doctors/me").then((res) => res.data);

export const updateMyDoctorProfile = (payload) =>
  apiClient.put("/doctors/me", payload).then((res) => res.data);

export const uploadMyProfilePhoto = (file) => {
  const data = new FormData();
  data.append("file", file);
  return apiClient.post("/doctors/me/profile-photo", data).then((res) => res.data);
};

export const listMyAvailability = () =>
  apiClient.get("/doctors/me/availability").then((res) => res.data);

export const addAvailability = (payload) =>
  apiClient.post("/doctors/me/availability", payload).then((res) => res.data);

export const deleteAvailability = (id) =>
  apiClient.delete(`/doctors/me/availability/${id}`).then((res) => res.data);
