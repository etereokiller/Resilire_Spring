import apiClient from "./apiClient";

export const listUsers = (page = 0, size = 20) =>
  apiClient.get(`/admin/users?page=${page}&size=${size}`).then((res) => res.data);

export const enableUser = (id) =>
  apiClient.put(`/admin/users/${id}/enable`).then((res) => res.data);

export const disableUser = (id) =>
  apiClient.put(`/admin/users/${id}/disable`).then((res) => res.data);
