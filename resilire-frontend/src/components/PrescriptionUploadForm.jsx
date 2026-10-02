import { useState } from "react";
import { useTranslation } from "react-i18next";
import * as prescriptionApi from "../api/prescriptionApi";

export default function PrescriptionUploadForm({ appointmentId, onUploaded }) {
  const { t } = useTranslation();
  const [file, setFile] = useState(null);
  const [notes, setNotes] = useState("");
  const [status, setStatus] = useState({ submitting: false, error: "", success: "" });

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!file) {
      setStatus({ submitting: false, error: t("prescriptionUpload.errorFileRequired"), success: "" });
      return;
    }
    setStatus({ submitting: true, error: "", success: "" });
    try {
      await prescriptionApi.uploadOfflinePrescription(appointmentId, file, notes);
      setStatus({ submitting: false, error: "", success: t("prescriptionUpload.success") });
      setFile(null);
      setNotes("");
      onUploaded?.();
    } catch (err) {
      setStatus({
        submitting: false,
        error: err.response?.data?.message || t("prescriptionUpload.errorFallback"),
        success: "",
      });
    }
  };

  return (
    <section className="panel">
      <h2>{t("prescriptionUpload.title")}</h2>
      <p className="muted-text">{t("prescriptionUpload.hint")}</p>
      {status.error && <p className="form-error">{status.error}</p>}
      {status.success && <p className="form-success">{status.success}</p>}
      <form onSubmit={handleSubmit} className="form">
        <div className="upload-dropzone">
          <p>{t("prescriptionUpload.selectFile")}</p>
          <input
            type="file"
            accept="application/pdf,image/jpeg,image/png"
            onChange={(e) => setFile(e.target.files?.[0] || null)}
          />
          {file && <p className="muted-text">{file.name}</p>}
        </div>
        <label>
          {t("prescriptionUpload.notes")}
          <textarea rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
        </label>
        <button type="submit" className="button primary" disabled={status.submitting}>
          {status.submitting ? t("prescriptionUpload.submitting") : t("prescriptionUpload.submit")}
        </button>
      </form>
    </section>
  );
}
