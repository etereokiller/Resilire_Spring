import axios from "axios";
import i18n from "../i18n/i18n";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("resilire_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Lets the backend return validation/error messages in the selected language.
  config.headers["Accept-Language"] = i18n.language;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("resilire_token");
      localStorage.removeItem("resilire_user");
    }
    return Promise.reject(error);
  }
);

export default apiClient;
