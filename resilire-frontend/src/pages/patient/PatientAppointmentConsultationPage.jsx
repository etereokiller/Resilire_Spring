import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as consultationApi from "../../api/consultationApi";
import * as prescriptionApi from "../../api/prescriptionApi";
import { downloadBlob } from "../../utils/downloadFile";
import ConsultationDetailCard from "../../components/ConsultationDetailCard";
import PrescriptionUploadForm from "../../components/PrescriptionUploadForm";

/**
 * Entry point from the patient's appointments table - unlike PatientConsultationDetailPage
 * (keyed by an existing consultation id), this is keyed by appointmentId since a consultation
 * record may not exist yet (the doctor never opened one and the patient never uploaded a
 * prescription for it).
 */
export default function PatientAppointmentConsultationPage() {
  const { appointmentId } = useParams();
  const { t } = useTranslation();
  const [consultation, setConsultation] = useState(null);
  const [prescription, setPrescription] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const consultations = await consultationApi.listMyConsultationsAsPatient();
      const match = consultations.find((c) => c.appointmentId === Number(appointmentId));
      if (!match) {
        setConsultation(null);
        setPrescription(null);
        setNotFound(true);
        return;
      }
      setNotFound(false);
      setConsultation(match);
      setPrescription(match.prescriptionStatus === "FINALIZED" ? await prescriptionApi.getPrescriptionAsPatient(match.id) : null);
    } catch (err) {
      setError(err.response?.data?.message || t("patientConsultationDetail.errorFallbackLoad"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appointmentId]);

  const handleDownload = async () => {
    setError("");
    try {
      const blob = await prescriptionApi.downloadPrescriptionAsPatient(consultation.id);
      const fileName =
        prescription?.type === "GENERATED"
          ? `prescription-${consultation.id}.pdf`
          : prescription?.fileName || "prescription";
      downloadBlob(blob, fileName);
    } catch {
      setError(t("patientConsultationDetail.errorFallbackDownload"));
    }
  };

  if (loading) {
    return <div className="content-page">{t("common.loading")}</div>;
  }

  const canUpload = !consultation || consultation.prescriptionStatus !== "FINALIZED";

  return (
    <div className="form-page wide">
      <Link to="/patient/appointments" className="muted-text">
        &larr; {t("patientConsultationDetail.backToAppointments")}
      </Link>
      <h1>{t("patientConsultationDetail.title")}</h1>
      {error && <p className="form-error">{error}</p>}

      {notFound || !consultation ? (
        <p className="muted-text">{t("patientConsultationDetail.noRecordYet")}</p>
      ) : (
        <ConsultationDetailCard consultation={consultation} prescription={prescription} onDownload={handleDownload} />
      )}

      {canUpload && <PrescriptionUploadForm appointmentId={Number(appointmentId)} onUploaded={load} />}
    </div>
  );
}
