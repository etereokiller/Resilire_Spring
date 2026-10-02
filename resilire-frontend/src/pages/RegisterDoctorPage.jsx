import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../context/AuthContext";

export default function RegisterDoctorPage() {
  const { t } = useTranslation();
  const { registerDoctor, loading } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    rut: "",
    firstName: "",
    surname1: "",
    surname2: "",
    phone: "",
    specialization: "",
    qualification: "",
    yearsOfExperience: "",
    consultationFee: "",
    bio: "",
  });
  const [error, setError] = useState("");

  const handleChange = (field) => (event) =>
    setForm((prev) => ({ ...prev, [field]: event.target.value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      await registerDoctor({
        ...form,
        yearsOfExperience: form.yearsOfExperience ? Number(form.yearsOfExperience) : null,
        consultationFee: form.consultationFee ? Number(form.consultationFee) : null,
      });
      navigate("/doctor/profile");
    } catch (err) {
      setError(err.response?.data?.message || t("registerDoctor.errorFallback"));
    }
  };

  return (
    <div className="registration-page">
      <aside className="registration-aside doctor-aside">
        <span className="auth-kicker">{t("registerDoctor.profileLabel")}</span>
        <h1>{t("registerDoctor.heroTitle")}</h1>
        <p>{t("registerDoctor.heroBody")}</p>
        <ol className="setup-steps"><li className="active"><span>1</span> {t("registerDoctor.step1")}</li><li className="active"><span>2</span> {t("registerDoctor.step2")}</li><li><span>3</span> {t("registerDoctor.step3")}</li></ol>
      </aside>
      <main className="registration-card doctor-registration-card">
        <div className="auth-card-heading"><span className="auth-kicker">{t("registerDoctor.joinLabel")}</span><h2>{t("registerDoctor.title")}</h2><p>{t("registerDoctor.intro")}</p></div>
        {error && <p className="form-error">{error}</p>}
        <form onSubmit={handleSubmit} className="form profile-form">
          <section className="profile-section"><div className="profile-section-heading"><span>01</span><div><h3>{t("registerDoctor.accountTitle")}</h3><p>{t("registerDoctor.accountBody")}</p></div></div><div className="form-grid">
        <label>
          {t("registerDoctor.email")}
          <input type="email" value={form.email} onChange={handleChange("email")} required />
        </label>
        <label>
          {t("registerDoctor.password")}
          <input
            type="password"
            value={form.password}
            onChange={handleChange("password")}
            minLength={8}
            required
          />
        </label>
        </div></section>
        <section className="profile-section"><div className="profile-section-heading"><span>02</span><div><h3>{t("registerDoctor.detailsTitle")}</h3><p>{t("registerDoctor.detailsBody")}</p></div></div><div className="form-grid">
        <label>
          {t("registerDoctor.rut")}
          <input
            value={form.rut}
            onChange={handleChange("rut")}
            placeholder="12.345.678-5"
            required
          />
        </label>
        <label>
          {t("registerDoctor.firstName")}
          <input value={form.firstName} onChange={handleChange("firstName")} required />
        </label>
        <label>
          {t("registerDoctor.surname1")}
          <input value={form.surname1} onChange={handleChange("surname1")} required />
        </label>
        <label>
          {t("registerDoctor.surname2")}
          <input value={form.surname2} onChange={handleChange("surname2")} required />
        </label>
        <label>
          {t("registerDoctor.phone")}
          <input value={form.phone} onChange={handleChange("phone")} />
        </label>
        <label>
          {t("registerDoctor.specialization")}
          <input value={form.specialization} onChange={handleChange("specialization")} />
        </label>
        <label>
          {t("registerDoctor.qualification")}
          <input value={form.qualification} onChange={handleChange("qualification")} />
        </label>
        <label>
          {t("registerDoctor.yearsOfExperience")}
          <input
            type="number"
            min="0"
            value={form.yearsOfExperience}
            onChange={handleChange("yearsOfExperience")}
          />
        </label>
        <label>
          {t("registerDoctor.consultationFee")}
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.consultationFee}
            onChange={handleChange("consultationFee")}
          />
        </label>
        <label className="form-grid-wide">
          {t("registerDoctor.bio")}
          <textarea value={form.bio} onChange={handleChange("bio")} rows={4} />
        </label>
        </div></section>
        <button type="submit" className="button primary auth-submit" disabled={loading}>
          {loading ? t("registerDoctor.submitting") : t("registerDoctor.submit")}
        </button>
      </form>
      <p className="auth-links">
        {t("registerDoctor.haveAccount")} <Link to="/login">{t("registerDoctor.logInHere")}</Link>
      </p>
      </main>
    </div>
  );
}
