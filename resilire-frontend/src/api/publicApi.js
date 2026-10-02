import apiClient from "./apiClient";

export const searchDoctors = ({ specialization = "", name = "", page = 0, size = 12 } = {}) =>
  apiClient
    .get("/public/doctors", { params: { specialization, name, page, size } })
    .then((res) => res.data);

export const getPublicDoctor = (id) =>
  apiClient.get(`/public/doctors/${id}`).then((res) => res.data);

export const getBookableSlots = (id, date) =>
  apiClient.get(`/public/doctors/${id}/bookable-slots`, { params: { date } }).then((res) => res.data);
