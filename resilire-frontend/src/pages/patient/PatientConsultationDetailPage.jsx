import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as consultationApi from "../../api/consultationApi";
import * as prescriptionApi from "../../api/prescriptionApi";
import { downloadBlob } from "../../utils/downloadFile";
import ConsultationDetailCard from "../../components/ConsultationDetailCard";
import PrescriptionUploadForm from "../../components/PrescriptionUploadForm";

export default function PatientConsultationDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation();
  const [consultation, setConsultation] = useState(null);
  const [prescription, setPrescription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      const c = await consultationApi.getConsultationAsPatient(id);
      setConsultation(c);
      if (c.prescriptionStatus === "FINALIZED") {
        setPrescription(await prescriptionApi.getPrescriptionAsPatient(id));
      } else {
        setPrescription(null);
      }
    } catch (err) {
      setError(err.response?.data?.message || t("patientConsultationDetail.errorFallbackLoad"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleDownload = async () => {
    setError("");
    try {
      const blob = await prescriptionApi.downloadPrescriptionAsPatient(id);
      const fileName =
        prescription?.type === "GENERATED"
          ? `prescription-${id}.pdf`
          : prescription?.fileName || "prescription";
      downloadBlob(blob, fileName);
    } catch {
      setError(t("patientConsultationDetail.errorFallbackDownload"));
    }
  };

  if (loading) {
    return <div className="content-page">{t("common.loading")}</div>;
  }
  if (!consultation) {
    return <div className="content-page">{error || t("patientConsultationDetail.notFound")}</div>;
  }

  const canUpload = consultation.appointmentStatus !== "CANCELLED" && consultation.prescriptionStatus !== "FINALIZED";

  return (
    <div className="form-page wide">
      <Link to="/patient/consultations" className="muted-text">
        &larr; {t("patientConsultationDetail.backToHistory")}
      </Link>
      <h1>{t("patientConsultationDetail.title")}</h1>
      {error && <p className="form-error">{error}</p>}

      <ConsultationDetailCard consultation={consultation} prescription={prescription} onDownload={handleDownload} />

      {canUpload && <PrescriptionUploadForm appointmentId={consultation.appointmentId} onUploaded={load} />}
    </div>
  );
}
