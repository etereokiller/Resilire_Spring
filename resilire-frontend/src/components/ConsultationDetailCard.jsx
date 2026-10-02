import { useTranslation } from "react-i18next";

export default function ConsultationDetailCard({ consultation, prescription, onDownload }) {
  const { t } = useTranslation();
  const isFinalized = prescription?.status === "FINALIZED";

  return (
    <>
      <div className="consultation-header-card">
        <div>
          <span className="consultation-header-label">{t("patientConsultationDetail.doctor")}</span>
          <span className="consultation-header-value">
            {t("common.doctorTitle")} {consultation.doctorName}
          </span>
          {consultation.doctorSpecialization && (
            <span className="muted-text">{consultation.doctorSpecialization}</span>
          )}
          <span className="muted-text">
            {t("patientConsultationDetail.doctorRut")}: {consultation.doctorRut}
          </span>
        </div>
        <div>
          <span className="consultation-header-label">{t("patientConsultationDetail.date")}</span>
          <span className="consultation-header-value">
            {consultation.appointmentDate} &middot; {consultation.startTime} - {consultation.endTime}
          </span>
        </div>
        <span className={`status-badge status-${consultation.status.toLowerCase()}`}>
          {t(`common.status.${consultation.status}`)}
        </span>
      </div>

      {consultation.status === "COMPLETED" ? (
        <section className="panel">
          <h2>{t("patientConsultationDetail.detailsTitle")}</h2>
          {consultation.chiefComplaint && (
            <div className="detail-section">
              <h3>{t("patientConsultationDetail.chiefComplaint")}</h3>
              <p>{consultation.chiefComplaint}</p>
            </div>
          )}
          {consultation.diagnosis && (
            <div className="detail-section">
              <h3>{t("patientConsultationDetail.diagnosis")}</h3>
              <p>{consultation.diagnosis}</p>
            </div>
          )}
          {consultation.notes && (
            <div className="detail-section">
              <h3>{t("patientConsultationDetail.notes")}</h3>
              <p>{consultation.notes}</p>
            </div>
          )}
          {!consultation.chiefComplaint && !consultation.diagnosis && !consultation.notes && (
            <p className="muted-text">{t("patientConsultationDetail.noDetails")}</p>
          )}
        </section>
      ) : (
        <p className="muted-text">{t("patientConsultationDetail.inProgress")}</p>
      )}

      {isFinalized && prescription ? (
        <section className="panel">
          <div className="panel-header">
            <h2>{t("patientConsultationDetail.prescriptionTitle")}</h2>
            <span className="status-badge status-completed">{t("patientConsultationDetail.available")}</span>
          </div>

          {prescription.type === "GENERATED" ? (
            <>
              <h3>{t("patientConsultationDetail.medicines")}</h3>
              {prescription.medicines.length === 0 ? (
                <p className="muted-text">{t("patientConsultationDetail.noMedicines")}</p>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>{t("patientConsultationDetail.medicineName")}</th>
                      <th>{t("patientConsultationDetail.dosage")}</th>
                      <th>{t("patientConsultationDetail.frequency")}</th>
                      <th>{t("patientConsultationDetail.duration")}</th>
                      <th>{t("patientConsultationDetail.instructions")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {prescription.medicines.map((m, i) => (
                      <tr key={i}>
                        <td>{m.medicineName}</td>
                        <td>{m.dosage}</td>
                        <td>{m.frequency}</td>
                        <td>{m.duration}</td>
                        <td>{m.instructions}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              <h3 style={{ marginTop: 20 }}>{t("patientConsultationDetail.tests")}</h3>
              {prescription.tests.length === 0 ? (
                <p className="muted-text">{t("patientConsultationDetail.noTests")}</p>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>{t("patientConsultationDetail.testName")}</th>
                      <th>{t("patientConsultationDetail.instructions")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {prescription.tests.map((item, i) => (
                      <tr key={i}>
                        <td>{item.testName}</td>
                        <td>{item.instructions}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {prescription.notes && (
                <div className="detail-section" style={{ marginTop: 20 }}>
                  <h3>{t("patientConsultationDetail.additionalNotes")}</h3>
                  <p>{prescription.notes}</p>
                </div>
              )}
            </>
          ) : (
            <p className="muted-text">
              {t("patientConsultationDetail.offlineFile")}: {prescription.fileName}
            </p>
          )}

          <div className="prescription-actions">
            <button type="button" className="button primary" onClick={onDownload}>
              {t("patientConsultationDetail.download")}
            </button>
          </div>
        </section>
      ) : (
        <p className="muted-text">{t("patientConsultationDetail.noPrescriptionYet")}</p>
      )}
    </>
  );
}
