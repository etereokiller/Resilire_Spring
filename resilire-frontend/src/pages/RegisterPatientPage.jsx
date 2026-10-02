import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../context/AuthContext";
import { isValidRut } from "../utils/rut";

export default function RegisterPatientPage() {
  const { t } = useTranslation();
  const { registerPatient, loading } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    rut: "",
    firstName: "",
    surname1: "",
    surname2: "",
    phone: "",
    dateOfBirth: "",
    gender: "",
    address: "",
  });
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  const handleChange = (field) => (event) => { setForm((prev) => ({ ...prev, [field]: event.target.value })); setFieldErrors((current) => ({ ...current, [field]: "" })); };
  const validateRut = () => setFieldErrors((current) => ({ ...current, rut: isValidRut(form.rut) ? "" : t("registrationValidation.rutInvalid") }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    const required = ["email", "password", "rut", "firstName", "surname1", "surname2"];
    const nextErrors = Object.fromEntries(required.filter((field) => !form[field].trim()).map((field) => [field, t("registrationValidation.required")]));
    if (form.rut && !isValidRut(form.rut)) nextErrors.rut = t("registrationValidation.rutInvalid");
    if (form.email && !/^\S+@\S+\.\S+$/.test(form.email)) nextErrors.email = t("registrationValidation.emailInvalid");
    if (form.password && form.password.length < 8) nextErrors.password = t("registrationValidation.passwordShort");
    setFieldErrors(nextErrors); if (Object.keys(nextErrors).length) return;
    try {
      await registerPatient({
        ...form,
        dateOfBirth: form.dateOfBirth || null,
      });
      navigate("/patient/profile");
    } catch (err) {
      setError(err.response?.data?.message || t("registerPatient.errorFallback"));
    }
  };

  return (
    <div className="registration-page">
      <aside className="registration-aside">
        <span className="auth-kicker">{t("registerPatient.profileLabel")}</span>
        <h1>{t("registerPatient.heroTitle")}</h1>
        <p>{t("registerPatient.heroBody")}</p>
        <ol className="setup-steps">
          <li className="active"><span>1</span> {t("registerPatient.step1")}</li>
          <li className="active"><span>2</span> {t("registerPatient.step2")}</li>
          <li><span>3</span> {t("registerPatient.step3")}</li>
        </ol>
      </aside>
      <main className="registration-card">
        <div className="auth-card-heading">
          <span className="auth-kicker">{t("registerPatient.createLabel")}</span>
          <h2>{t("registerPatient.title")}</h2>
          <p>{t("registerPatient.requiredHint")}</p>
        </div>
        {error && <p className="form-error">{error}</p>}
        <form onSubmit={handleSubmit} className="form profile-form">
          <section className="profile-section">
            <div className="profile-section-heading"><span>01</span><div><h3>{t("registerPatient.accountTitle")}</h3><p>{t("registerPatient.accountBody")}</p></div></div>
            <div className="form-grid">
              <label>{t("registerPatient.email")}<input className={fieldErrors.email ? "input-error" : ""} type="email" value={form.email} onChange={handleChange("email")} required />{fieldErrors.email && <small className="field-error">{fieldErrors.email}</small>}</label>
              <label>{t("registerPatient.password")}<input className={fieldErrors.password ? "input-error" : ""} type="password" value={form.password} onChange={handleChange("password")} minLength={8} required />{fieldErrors.password && <small className="field-error">{fieldErrors.password}</small>}</label>
              <label>{t("registerPatient.rut")}<input className={fieldErrors.rut ? "input-error" : ""} value={form.rut} onChange={handleChange("rut")} onBlur={validateRut} placeholder="12.345.678-5" required />{fieldErrors.rut && <small className="field-error">{fieldErrors.rut}</small>}</label>
            </div>
          </section>
          <section className="profile-section">
            <div className="profile-section-heading"><span>02</span><div><h3>{t("registerPatient.detailsTitle")}</h3><p>{t("registerPatient.detailsBody")}</p></div></div>
            <div className="form-grid">
              <label>{t("registerPatient.firstName")}<input className={fieldErrors.firstName ? "input-error" : ""} value={form.firstName} onChange={handleChange("firstName")} required />{fieldErrors.firstName && <small className="field-error">{fieldErrors.firstName}</small>}</label>
              <label>{t("registerPatient.surname1")}<input className={fieldErrors.surname1 ? "input-error" : ""} value={form.surname1} onChange={handleChange("surname1")} required />{fieldErrors.surname1 && <small className="field-error">{fieldErrors.surname1}</small>}</label>
              <label>{t("registerPatient.surname2")}<input className={fieldErrors.surname2 ? "input-error" : ""} value={form.surname2} onChange={handleChange("surname2")} required />{fieldErrors.surname2 && <small className="field-error">{fieldErrors.surname2}</small>}</label>
              <label>{t("registerPatient.phone")}<input value={form.phone} onChange={handleChange("phone")} /></label>
              <label>{t("registerPatient.dateOfBirth")}<input type="date" value={form.dateOfBirth} onChange={handleChange("dateOfBirth")} /></label>
              <label>{t("registerPatient.gender")}<select value={form.gender} onChange={handleChange("gender")}><option value="">{t("common.select")}</option><option value="Female">{t("common.genderFemale")}</option><option value="Male">{t("common.genderMale")}</option><option value="Other">{t("common.genderOther")}</option></select></label>
              <label className="form-grid-wide">{t("registerPatient.address")}<input value={form.address} onChange={handleChange("address")} /></label>
            </div>
          </section>
          <button type="submit" className="button primary auth-submit" disabled={loading}>{loading ? t("registerPatient.submitting") : t("registerPatient.submit")}</button>
        </form>
        <p className="auth-links">{t("registerPatient.haveAccount")} <Link to="/login">{t("registerPatient.logInHere")}</Link></p>
      </main>
    </div>
  );
}
