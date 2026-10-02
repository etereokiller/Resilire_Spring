import { Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import ProtectedRoute from "./components/ProtectedRoute";
import LandingPage from "./pages/LandingPage";
import AboutPage from "./pages/AboutPage";
import HelpPage from "./pages/HelpPage";
import PrivacyPage from "./pages/PrivacyPage";
import LoginPage from "./pages/LoginPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import RegisterPatientPage from "./pages/RegisterPatientPage";
import RegisterDoctorPage from "./pages/RegisterDoctorPage";
import DoctorSearchPage from "./pages/DoctorSearchPage";
import DoctorPublicProfilePage from "./pages/DoctorPublicProfilePage";
import PatientProfilePage from "./pages/patient/PatientProfilePage";
import PatientAppointmentsPage from "./pages/patient/PatientAppointmentsPage";
import PatientConsultationHistoryPage from "./pages/patient/PatientConsultationHistoryPage";
import PatientConsultationDetailPage from "./pages/patient/PatientConsultationDetailPage";
import PatientAppointmentConsultationPage from "./pages/patient/PatientAppointmentConsultationPage";
import DoctorProfilePage from "./pages/doctor/DoctorProfilePage";
import DoctorAvailabilityPage from "./pages/doctor/DoctorAvailabilityPage";
import DoctorAppointmentsPage from "./pages/doctor/DoctorAppointmentsPage";
import DoctorConsultationPage from "./pages/doctor/DoctorConsultationPage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";
import NotFoundPage from "./pages/NotFoundPage";

export default function App() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-main">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/help" element={<HelpPage />} />
          <Route path="/privacy" element={<PrivacyPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/register/patient" element={<RegisterPatientPage />} />
          <Route path="/register/doctor" element={<RegisterDoctorPage />} />
          <Route path="/doctors" element={<DoctorSearchPage />} />
          <Route path="/doctors/:id" element={<DoctorPublicProfilePage />} />

          <Route
            path="/patient/profile"
            element={
              <ProtectedRoute allowedRoles={["PATIENT"]}>
                <PatientProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patient/appointments"
            element={
              <ProtectedRoute allowedRoles={["PATIENT"]}>
                <PatientAppointmentsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patient/appointments/:appointmentId/consultation"
            element={
              <ProtectedRoute allowedRoles={["PATIENT"]}>
                <PatientAppointmentConsultationPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patient/consultations"
            element={
              <ProtectedRoute allowedRoles={["PATIENT"]}>
                <PatientConsultationHistoryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patient/consultations/:id"
            element={
              <ProtectedRoute allowedRoles={["PATIENT"]}>
                <PatientConsultationDetailPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/doctor/profile"
            element={
              <ProtectedRoute allowedRoles={["DOCTOR"]}>
                <DoctorProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/doctor/availability"
            element={
              <ProtectedRoute allowedRoles={["DOCTOR"]}>
                <DoctorAvailabilityPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/doctor/appointments"
            element={
              <ProtectedRoute allowedRoles={["DOCTOR"]}>
                <DoctorAppointmentsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/doctor/appointments/:appointmentId/consultation"
            element={
              <ProtectedRoute allowedRoles={["DOCTOR"]}>
                <DoctorConsultationPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/users"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <AdminUsersPage />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
