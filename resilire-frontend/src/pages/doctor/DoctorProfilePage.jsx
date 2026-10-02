import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as doctorApi from "../../api/doctorApi";

export default function DoctorProfilePage() {
  const { t } = useTranslation();
  const [form, setForm] = useState(null);
  const [savedProfile, setSavedProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [status, setStatus] = useState({ loading: true, error: "", success: "" });
  const photoUrl = form?.hasProfilePhoto ? `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api"}/public/doctors/${form.id}/photo?updated=${Date.now()}` : null;

  useEffect(() => {
    doctorApi
      .getMyDoctorProfile()
      .then((data) => {
        setForm(data);
        setSavedProfile(data);
      })
      .catch(() => setStatus((s) => ({ ...s, error: t("doctorProfile.errorFallbackLoad") })))
      .finally(() => setStatus((s) => ({ ...s, loading: false })));
  }, []);

  const handleChange = (field) => (event) =>
    setForm((prev) => ({ ...prev, [field]: event.target.value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus({ loading: false, error: "", success: "" });
    try {
      const updated = await doctorApi.updateMyDoctorProfile({
        ...form,
        yearsOfExperience: form.yearsOfExperience ? Number(form.yearsOfExperience) : null,
        consultationFee: form.consultationFee ? Number(form.consultationFee) : null,
      });
      setForm(updated);
      setSavedProfile(updated);
      setIsEditing(false);
      setStatus({ loading: false, error: "", success: t("doctorProfile.successUpdate") });
    } catch (err) {
      setStatus({
        loading: false,
        error: err.response?.data?.message || t("doctorProfile.errorFallbackUpdate"),
        success: "",
      });
    }
  };

  const handlePhotoUpload = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setStatus({ loading: false, error: "", success: "" });
    try {
      const updated = await doctorApi.uploadMyProfilePhoto(file);
      setForm(updated); setSavedProfile(updated);
      setStatus({ loading: false, error: "", success: t("doctorProfile.photoUpdated") });
    } catch (err) { setStatus({ loading: false, success: "", error: err.response?.data?.message || t("doctorProfile.photoError") }); }
  };

  if (status.loading || !form) {
    return <div className="content-page">{t("common.loadingProfile")}</div>;
  }

  return (
    <div className="content-page profile-page doctor-profile-page">
      <div className="profile-hero">
        <div className="profile-avatar doctor-profile-avatar">{photoUrl ? <img src={photoUrl} alt="" /> : "Dr"}</div>
        <div className="profile-identity"><span className="profile-eyebrow">CLINICIAN PROFILE</span><h1>Dr. {form.firstName} {form.surname1} {form.surname2}</h1><p>{form.specialization || "Healthcare professional"} · {form.email}</p></div>
        <div className="profile-hero-actions"><Link to="/doctor/availability" className="button secondary">{t("doctorProfile.manageAvailability")}</Link><button className="button primary" onClick={() => setIsEditing(true)}>{t("common.editProfile")}</button></div>
      </div>
      {status.error && <p className="form-error">{status.error}</p>}
      {status.success && <p className="form-success">{status.success}</p>}
      <label className="photo-upload-control">{t("doctorProfile.profilePhoto")}<input type="file" accept="image/png,image/jpeg,image/webp" onChange={handlePhotoUpload} /><small>{t("doctorProfile.photoHint")}</small></label>
      {!isEditing ? (
        <section className="profile-details-card">
          <div className="profile-details-heading"><h2>Professional profile</h2><p>Information patients see when choosing their clinician.</p></div>
          <dl className="profile-details-grid">
            <div><dt>{t("doctorProfile.rut")}</dt><dd>{form.rut || "—"}</dd></div>
            <div><dt>{t("doctorProfile.phone")}</dt><dd>{form.phone || "—"}</dd></div>
            <div><dt>{t("doctorProfile.specialization")}</dt><dd>{form.specialization || "—"}</dd></div>
            <div><dt>{t("doctorProfile.qualification")}</dt><dd>{form.qualification || "—"}</dd></div>
            <div><dt>{t("doctorProfile.yearsOfExperience")}</dt><dd>{form.yearsOfExperience ?? "—"}</dd></div>
            <div><dt>{t("doctorProfile.consultationFee")}</dt><dd>{form.consultationFee ?? "—"}</dd></div>
            <div className="profile-details-wide"><dt>{t("doctorProfile.bio")}</dt><dd className="profile-bio">{form.bio || "—"}</dd></div>
          </dl>
        </section>
      ) : <form onSubmit={handleSubmit} className="form profile-form profile-edit-form">
        <div className="profile-section-heading"><span>EDIT</span><div><h3>Update professional profile</h3><p>Make changes to the information patients use to find you.</p></div></div>
        <div className="form-grid">
        <label>
          {t("doctorProfile.firstName")}
          <input value={form.firstName || ""} onChange={handleChange("firstName")} required />
        </label>
        <label>
          {t("doctorProfile.surname1")}
          <input value={form.surname1 || ""} onChange={handleChange("surname1")} required />
        </label>
        <label>
          {t("doctorProfile.surname2")}
          <input value={form.surname2 || ""} onChange={handleChange("surname2")} required />
        </label>
        <label>
          {t("doctorProfile.phone")}
          <input value={form.phone || ""} onChange={handleChange("phone")} />
        </label>
        <label>
          {t("doctorProfile.specialization")}
          <input value={form.specialization || ""} onChange={handleChange("specialization")} />
        </label>
        <label>
          {t("doctorProfile.qualification")}
          <input value={form.qualification || ""} onChange={handleChange("qualification")} />
        </label>
        <label>
          {t("doctorProfile.yearsOfExperience")}
          <input
            type="number"
            min="0"
            value={form.yearsOfExperience ?? ""}
            onChange={handleChange("yearsOfExperience")}
          />
        </label>
        <label>
          {t("doctorProfile.consultationFee")}
          <input
            type="number"
            min="0"
            step="0.01"
            value={form.consultationFee ?? ""}
            onChange={handleChange("consultationFee")}
          />
        </label>
        <label className="form-grid-wide">
          {t("doctorProfile.bio")}
          <textarea value={form.bio || ""} onChange={handleChange("bio")} rows={4} />
        </label>
        </div>
        <div className="profile-edit-actions"><button type="button" className="button secondary" onClick={() => { setForm(savedProfile); setIsEditing(false); }}>Cancel</button><button type="submit" className="button primary">
          {t("doctorProfile.save")}
        </button></div>
      </form>
      }
    </div>
  );
}
