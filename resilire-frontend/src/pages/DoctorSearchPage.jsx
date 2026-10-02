import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as publicApi from "../api/publicApi";

export default function DoctorSearchPage() {
  const { t } = useTranslation();
  const [specialization, setSpecialization] = useState("");
  const [name, setName] = useState("");
  const [doctors, setDoctors] = useState([]);
  const [error, setError] = useState("");

  const runSearch = (event) => {
    event?.preventDefault();
    setError("");
    publicApi
      .searchDoctors({ specialization, name })
      .then((page) => setDoctors(page.content))
      .catch(() => setError(t("doctorSearch.errorFallback")));
  };

  useEffect(() => {
    runSearch();
  }, []);

  return (
    <div className="content-page wide">
      <h1>{t("doctorSearch.title")}</h1>

      <form onSubmit={runSearch} className="form inline-form">
        <label>
          {t("doctorSearch.specialization")}
          <input
            value={specialization}
            onChange={(e) => setSpecialization(e.target.value)}
            placeholder={t("doctorSearch.specializationPlaceholder")}
          />
        </label>
        <label>
          {t("doctorSearch.name")}
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder={t("doctorSearch.namePlaceholder")}
          />
        </label>
        <button type="submit" className="button primary">
          {t("doctorSearch.search")}
        </button>
      </form>

      {error && <p className="form-error">{error}</p>}

      <div className="feature-grid">
        {doctors.length === 0 && <p>{t("doctorSearch.noResults")}</p>}
        {doctors.map((doctor) => (
          <div key={doctor.id} className="feature-card">
            <h3>
              {t("common.doctorTitle")} {doctor.firstName} {doctor.surname1} {doctor.surname2}
            </h3>
            <p className="muted">{doctor.specialization || t("common.generalSpecialization")}</p>
            <p>{doctor.qualification}</p>
            <p>
              {doctor.yearsOfExperience != null &&
                t("doctorSearch.yearsExperience", { years: doctor.yearsOfExperience })}
              {doctor.consultationFee != null && ` · $${doctor.consultationFee}`}
            </p>
            <Link to={`/doctors/${doctor.id}`} className="button secondary">
              {t("doctorSearch.viewProfile")}
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}
