import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../context/AuthContext";
import LanguageToggle from "./LanguageToggle";
import ResiLogo from "./ResiLogo";

export default function Navbar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const dashboardLink =
    user?.role === "PATIENT"
      ? "/patient/profile"
      : user?.role === "DOCTOR"
      ? "/doctor/profile"
      : user?.role === "ADMIN"
      ? "/admin/users"
      : "/";

  const appointmentsLink =
    user?.role === "PATIENT"
      ? "/patient/appointments"
      : user?.role === "DOCTOR"
      ? "/doctor/appointments"
      : null;

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          <ResiLogo width={120} />
        </Link>
        <nav className="nav-links">
          <Link to="/doctors">{t("nav.findDoctors")}</Link>
          <Link to="/about">{t("nav.about")}</Link>
          <Link to="/help">{t("nav.help")}</Link>
          {user ? (
            <>
              {appointmentsLink && <Link to={appointmentsLink}>{t("nav.myAppointments")}</Link>}
              {user?.role === "PATIENT" && (
                <Link to="/patient/consultations">{t("nav.myConsultations")}</Link>
              )}
              <Link to={dashboardLink}>{t("nav.myAccount")}</Link>
              <span className="user-chip" title={user.email}><span className="user-chip-avatar">{user.email?.[0]?.toUpperCase()}</span><span>{user.email}</span></span>
              <button className="link-button" onClick={handleLogout}>
                {t("nav.logout")}
              </button>
            </>
          ) : (
            <>
              <Link to="/login">{t("nav.login")}</Link>
              <Link to="/register/patient" className="cta-link">
                {t("nav.getStarted")}
              </Link>
            </>
          )}
          <LanguageToggle />
        </nav>
      </div>
    </header>
  );
}
