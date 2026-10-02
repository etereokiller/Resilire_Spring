import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as consultationApi from "../../api/consultationApi";

export default function PatientConsultationHistoryPage() {
  const { t } = useTranslation();
  const [consultations, setConsultations] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    consultationApi
      .listMyConsultationsAsPatient()
      .then(setConsultations)
      .catch(() => setError(t("patientConsultations.errorFallbackLoad")))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) {
    return <div className="content-page">{t("common.loading")}</div>;
  }

  return (
    <div className="form-page wide">
      <h1>{t("patientConsultations.title")}</h1>
      <p className="muted">{t("patientConsultations.subtitle")}</p>
      {error && <p className="form-error">{error}</p>}
      {consultations.length === 0 ? (
        <p className="muted-text">{t("patientConsultations.noConsultations")}</p>
      ) : (
        <div className="consultation-card-list">
          {consultations.map((c) => (
            <div className="consultation-card" key={c.id}>
              <div className="consultation-card-main">
                <strong>
                  {t("common.doctorTitle")} {c.doctorName}
                </strong>
                <span className="muted-text">{c.doctorSpecialization}</span>
                <span className="muted-text">
                  {c.appointmentDate} &middot; {c.startTime} - {c.endTime}
                </span>
              </div>
              <div className="consultation-card-status">
                <span className={`status-badge status-${c.status.toLowerCase()}`}>
                  {t(`common.status.${c.status}`)}
                </span>
                {c.prescriptionStatus === "FINALIZED" && (
                  <span className="status-badge status-completed">
                    {t("patientConsultations.prescriptionAvailable")}
                  </span>
                )}
              </div>
              <Link className="button secondary" to={`/patient/consultations/${c.id}`}>
                {t("patientConsultations.viewDetails")}
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
