import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../context/AuthContext";

const dashboardByRole = {
  PATIENT: "/patient/appointments",
  DOCTOR: "/doctor/appointments",
  ADMIN: "/admin/users",
};

export default function LoginPage() {
  const { t } = useTranslation();
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const user = await login(email, password);
      navigate(dashboardByRole[user.role] || "/");
    } catch (err) {
      setError(err.response?.data?.message || t("login.errorFallback"));
    }
  };

  return (
    <div className="auth-page">
      <aside className="auth-aside">
        <span className="auth-kicker">{t("login.careLabel")}</span>
        <h1>{t("login.heroTitle")}</h1>
        <p>{t("login.heroBody")}</p>
        <div className="auth-benefits">
          <span>{t("login.benefit1")}</span>
          <span>{t("login.benefit2")}</span>
          <span>{t("login.benefit3")}</span>
        </div>
      </aside>
      <main className="auth-card auth-login-card">
        <div className="auth-card-heading">
          <span className="auth-kicker">{t("login.welcomeLabel")}</span>
          <h2>{t("login.title")}</h2>
          <p>{t("login.intro")}</p>
        </div>
        {error && <p className="form-error">{error}</p>}
        <form onSubmit={handleSubmit} className="form auth-form">
          <label>
            {t("login.email")}
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <p className="auth-recovery-link"><Link to="/forgot-password">{t("login.forgotPassword")}</Link></p>
          <label>
            {t("login.password")}
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <button type="submit" className="button primary auth-submit" disabled={loading}>
            {loading ? t("login.submitting") : t("login.submit")}
          </button>
        </form>
        <div className="auth-links">
          <p>{t("login.newPatient")} <Link to="/register/patient">{t("login.registerHere")}</Link></p>
          <p>{t("login.newDoctor")} <Link to="/register/doctor">{t("login.registerHere")}</Link></p>
        </div>
      </main>
    </div>
  );
}
