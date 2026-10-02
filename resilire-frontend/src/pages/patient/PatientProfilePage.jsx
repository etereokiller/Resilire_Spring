import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as patientApi from "../../api/patientApi";

export default function PatientProfilePage() {
  const { t } = useTranslation();
  const [form, setForm] = useState(null);
  const [savedProfile, setSavedProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [status, setStatus] = useState({ loading: true, error: "", success: "" });

  useEffect(() => {
    patientApi
      .getMyPatientProfile()
      .then((data) => {
        setForm(data);
        setSavedProfile(data);
      })
      .catch(() => setStatus((s) => ({ ...s, error: t("patientProfile.errorFallbackLoad") })))
      .finally(() => setStatus((s) => ({ ...s, loading: false })));
  }, []);

  const handleChange = (field) => (event) =>
    setForm((prev) => ({ ...prev, [field]: event.target.value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus({ loading: false, error: "", success: "" });
    try {
      const updated = await patientApi.updateMyPatientProfile(form);
      setForm(updated);
      setSavedProfile(updated);
      setIsEditing(false);
      setStatus({ loading: false, error: "", success: t("patientProfile.successUpdate") });
    } catch (err) {
      setStatus({
        loading: false,
        error: err.response?.data?.message || t("patientProfile.errorFallbackUpdate"),
        success: "",
      });
    }
  };

  if (status.loading || !form) {
    return <div className="content-page">{t("common.loadingProfile")}</div>;
  }

  return (
    <div className="content-page profile-page">
      <div className="profile-hero">
        <div className="profile-avatar" aria-hidden="true">{`${form.firstName?.[0] || ""}${form.surname1?.[0] || ""}`}</div>
        <div className="profile-identity"><span className="profile-eyebrow">PATIENT PROFILE</span><h1>{form.firstName} {form.surname1} {form.surname2}</h1><p>{form.email}</p></div>
        <button className="button secondary profile-edit-button" onClick={() => setIsEditing(true)}>{t("common.editProfile")}</button>
      </div>
      {status.error && <p className="form-error">{status.error}</p>}
      {status.success && <p className="form-success">{status.success}</p>}
      {!isEditing ? (
        <section className="profile-details-card">
          <div className="profile-details-heading"><h2>Personal details</h2><p>Your private profile information.</p></div>
          <dl className="profile-details-grid">
            <div><dt>{t("patientProfile.rut")}</dt><dd>{form.rut || "—"}</dd></div>
            <div><dt>{t("patientProfile.phone")}</dt><dd>{form.phone || "—"}</dd></div>
            <div><dt>{t("patientProfile.dateOfBirth")}</dt><dd>{form.dateOfBirth || "—"}</dd></div>
            <div><dt>{t("patientProfile.gender")}</dt><dd>{form.gender || "—"}</dd></div>
            <div className="profile-details-wide"><dt>{t("patientProfile.address")}</dt><dd>{form.address || "—"}</dd></div>
          </dl>
        </section>
      ) : <form onSubmit={handleSubmit} className="form profile-form profile-edit-form">
        <div className="profile-section-heading"><span>EDIT</span><div><h3>Update your details</h3><p>Keep your profile information current for a better care experience.</p></div></div>
        <div className="form-grid">
        <label>
          {t("patientProfile.firstName")}
          <input value={form.firstName || ""} onChange={handleChange("firstName")} required />
        </label>
        <label>
          {t("patientProfile.surname1")}
          <input value={form.surname1 || ""} onChange={handleChange("surname1")} required />
        </label><label>
          {t("patientProfile.surname2")}
          <input value={form.surname2 || ""} onChange={handleChange("surname2")} required />
        </label>
        <label>
          {t("patientProfile.phone")}
          <input value={form.phone || ""} onChange={handleChange("phone")} />
        </label>
        <label>
          {t("patientProfile.dateOfBirth")}
          <input
            type="date"
            value={form.dateOfBirth || ""}
            onChange={handleChange("dateOfBirth")}
          />
        </label>
        <label>
          {t("patientProfile.gender")}
          <select value={form.gender || ""} onChange={handleChange("gender")}>
            <option value="">{t("common.select")}</option>
            <option value="Female">{t("common.genderFemale")}</option>
            <option value="Male">{t("common.genderMale")}</option>
            <option value="Other">{t("common.genderOther")}</option>
          </select>
        </label>
        <label>
          {t("patientProfile.address")}
          <input value={form.address || ""} onChange={handleChange("address")} />
        </label>
        </div>
        <div className="profile-edit-actions">
          <button type="button" className="button secondary" onClick={() => { setForm(savedProfile); setIsEditing(false); }}>Cancel</button>
        <button type="submit" className="button primary">
          {t("patientProfile.save")}
        </button>
        </div>
      </form>
      }
    </div>
  );
}
