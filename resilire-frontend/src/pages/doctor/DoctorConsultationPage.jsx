import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as consultationApi from "../../api/consultationApi";
import * as prescriptionApi from "../../api/prescriptionApi";
import { downloadBlob } from "../../utils/downloadFile";
import cie10Catalogue from "../../data/cie10-diagnosticos-espanol.json";
import medicineCatalogue from "../../data/medicine-catalogue.json";

const emptyMedicine = () => ({ medicineName: "", dosage: "", frequency: "", duration: "", instructions: "" });
const emptyTest = () => ({ testName: "", instructions: "" });

export default function DoctorConsultationPage() {
  const { appointmentId } = useParams();
  const { t } = useTranslation();

  const [consultation, setConsultation] = useState(null);
  const [details, setDetails] = useState({ chiefComplaint: "", diagnosis: "", notes: "", medicalHistory: "", surgicalHistory: "", psychiatricHistory: "", familyHistory: "", tobaccoUse: null, tobaccoDetails: "", alcoholUse: null, alcoholDetails: "", drugUse: null, drugDetails: "", anamnesis: "", mentalExam: "", cie10Diagnosis: "", indications: "", certificateReason: "" });
  const [prescription, setPrescription] = useState(null);
  const [medicines, setMedicines] = useState([emptyMedicine()]);
  const [tests, setTests] = useState([]);
  const [prescriptionNotes, setPrescriptionNotes] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    let ignore = false;

    const load = async () => {
      try {
        const c = await consultationApi.openConsultation(Number(appointmentId));
        if (ignore) return;
        setConsultation(c);
        setDetails((prev) => Object.fromEntries(Object.keys(prev).map((key) => [key, c[key] ?? ""])));

        const p = await prescriptionApi.getPrescriptionAsDoctor(c.id);
        if (ignore) return;
        applyPrescription(p);
      } catch (err) {
        if (!ignore) {
          setError(err.response?.data?.message || t("doctorConsultation.errorFallbackLoad"));
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    load();
    return () => {
      ignore = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appointmentId]);

  const applyPrescription = (p) => {
    setPrescription(p);
    setMedicines(p.medicines?.length ? p.medicines : [emptyMedicine()]);
    setTests(p.tests || []);
    setPrescriptionNotes(p.notes || "");
  };

  const isLocked = consultation?.status === "COMPLETED";
  const isFinalized = prescription?.status === "FINALIZED";

  const handleDetailsChange = (field) => (event) =>
    setDetails((prev) => ({ ...prev, [field]: event.target.value }));

  const handleSaveDetails = async (event) => {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      const updated = await consultationApi.updateConsultationDetails(consultation.id, details);
      setConsultation(updated);
      setSuccess(t("doctorConsultation.detailsSaved"));
    } catch (err) {
      setError(err.response?.data?.message || t("doctorConsultation.errorFallbackSaveDetails"));
    }
  };

  const updateMedicine = (index, field) => (event) =>
    setMedicines((prev) => prev.map((m, i) => (i === index ? { ...m, [field]: event.target.value } : m)));

  const updateTest = (index, field) => (event) =>
    setTests((prev) => prev.map((item, i) => (i === index ? { ...item, [field]: event.target.value } : item)));

  const addMedicineRow = () => setMedicines((prev) => [...prev, emptyMedicine()]);
  const removeMedicineRow = (index) => setMedicines((prev) => prev.filter((_, i) => i !== index));
  const addTestRow = () => setTests((prev) => [...prev, emptyTest()]);
  const removeTestRow = (index) => setTests((prev) => prev.filter((_, i) => i !== index));

  const buildDraftPayload = () => ({
    notes: prescriptionNotes,
    medicines: medicines.filter((m) => m.medicineName.trim()),
    tests: tests.filter((item) => item.testName.trim()),
  });

  const saveDraftInternal = async () => {
    const updated = await prescriptionApi.savePrescriptionDraft(consultation.id, buildDraftPayload());
    applyPrescription(updated);
    return updated;
  };

  const handleSaveDraft = async () => {
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      await saveDraftInternal();
      setSuccess(t("doctorConsultation.draftSaved"));
    } catch (err) {
      setError(err.response?.data?.message || t("doctorConsultation.errorFallbackSaveDraft"));
    } finally {
      setSaving(false);
    }
  };

  const handleFinalize = async () => {
    if (!window.confirm(t("doctorConsultation.confirmFinalize"))) {
      return;
    }
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      await saveDraftInternal();
      const finalized = await prescriptionApi.finalizePrescription(consultation.id);
      applyPrescription(finalized);
      const refreshedConsultation = await consultationApi.getConsultationAsDoctor(consultation.id);
      setConsultation(refreshedConsultation);
      setSuccess(t("doctorConsultation.prescriptionFinalized"));
    } catch (err) {
      setError(err.response?.data?.message || t("doctorConsultation.errorFallbackFinalize"));
    } finally {
      setSaving(false);
    }
  };

  const handleDownload = async () => {
    setError("");
    try {
      const blob = await prescriptionApi.downloadPrescriptionAsDoctor(consultation.id);
      downloadBlob(blob, `prescription-${consultation.id}.pdf`);
    } catch {
      setError(t("doctorConsultation.errorFallbackDownload"));
    }
  };

  const handleCertificateDownload = async () => {
    setError("");
    try {
      if (!isLocked) {
        const updated = await consultationApi.updateConsultationDetails(consultation.id, details);
        setConsultation(updated);
      }
      const blob = await prescriptionApi.downloadMedicalCertificateAsDoctor(consultation.id);
      downloadBlob(blob, `medical-certificate-${consultation.id}.pdf`);
    } catch (err) {
      setError(err.response?.data?.message || t("doctorConsultation.errorFallbackDownload"));
    }
  };

  if (loading || !consultation) {
    return <div className="content-page">{error || t("common.loading")}</div>;
  }

  return (
    <div className="form-page wide">
      <Link to="/doctor/appointments" className="muted-text">
        &larr; {t("doctorConsultation.backToAppointments")}
      </Link>
      <h1>{t("doctorConsultation.title")}</h1>

      <div className="consultation-header-card">
        <div>
          <span className="consultation-header-label">{t("doctorConsultation.patient")}</span>
          <span className="consultation-header-value">{consultation.patientName}</span>
          <span className="muted-text">
            {t("doctorConsultation.patientRut")}: {consultation.patientRut}
          </span>
        </div>
        <div>
          <span className="consultation-header-label">{t("doctorConsultation.date")}</span>
          <span className="consultation-header-value">
            {consultation.appointmentDate} &middot; {consultation.startTime} - {consultation.endTime}
          </span>
        </div>
        <span className={`status-badge status-${consultation.status.toLowerCase()}`}>
          {t(`common.status.${consultation.status}`)}
        </span>
      </div>

      {error && <p className="form-error">{error}</p>}
      {success && <p className="form-success">{success}</p>}

      <section className="panel">
        <h2>{t("doctorConsultation.detailsTitle")}</h2>
        <form onSubmit={handleSaveDetails} className="form">
          <label>
            {t("doctorConsultation.chiefComplaint")}
            <textarea
              rows={2}
              maxLength={1000}
              value={details.chiefComplaint}
              onChange={handleDetailsChange("chiefComplaint")}
              disabled={isLocked}
            />
          </label>
          <label>
            {t("doctorConsultation.diagnosis")}
            <textarea
              rows={2}
              maxLength={1000}
              value={details.diagnosis}
              onChange={handleDetailsChange("diagnosis")}
              disabled={isLocked}
            />
          </label>
          <label>
            {t("doctorConsultation.notes")}
            <textarea
              rows={4}
              maxLength={4000}
              value={details.notes}
              onChange={handleDetailsChange("notes")}
              disabled={isLocked}
            />
          </label>
          <div className="clinical-grid">
            {[
              ["medicalHistory", "medicalHistory"], ["surgicalHistory", "surgicalHistory"],
              ["psychiatricHistory", "psychiatricHistory"], ["familyHistory", "familyHistory"],
            ].map(([field, label]) => <label key={field}>{t(`doctorConsultation.${label}`)}<textarea rows={3} maxLength={2000} value={details[field]} onChange={handleDetailsChange(field)} disabled={isLocked} /></label>)}
          </div>
          <fieldset className="clinical-fieldset">
            <legend>{t("doctorConsultation.consumption")}</legend>
            <div className="clinical-grid consumption-grid">
              {["tobaccoUse", "alcoholUse", "drugUse"].map((field) => <div key={field}><label>{t(`doctorConsultation.${field}`)}<select value={details[field] === null || details[field] === "" ? "" : String(details[field])} onChange={(e) => setDetails((prev) => ({ ...prev, [field]: e.target.value === "" ? null : e.target.value === "true" }))} disabled={isLocked}><option value="">—</option><option value="true">{t("doctorConsultation.yes")}</option><option value="false">{t("doctorConsultation.no")}</option></select></label>{details[field] === true && <label className="substance-detail">{t(`doctorConsultation.${field.replace("Use", "Details")}`)}<input maxLength={1000} value={details[field.replace("Use", "Details")]} onChange={handleDetailsChange(field.replace("Use", "Details"))} disabled={isLocked} /></label>}</div>)}
            </div>
          </fieldset>
          <label>{t("doctorConsultation.anamnesis")}<textarea rows={4} maxLength={4000} value={details.anamnesis} onChange={handleDetailsChange("anamnesis")} disabled={isLocked} /></label>
          <label>{t("doctorConsultation.mentalExam")}<textarea rows={4} maxLength={4000} value={details.mentalExam} onChange={handleDetailsChange("mentalExam")} disabled={isLocked} /></label>
          <label>{t("doctorConsultation.cie10Diagnosis")}<input list="cie10-options" maxLength={500} placeholder={t("doctorConsultation.cie10Placeholder")} value={details.cie10Diagnosis} onChange={handleDetailsChange("cie10Diagnosis")} disabled={isLocked} /><datalist id="cie10-options">{cie10Catalogue.diagnosticos.map((item) => <option key={item.codigo} value={`${item.codigo} — ${item.descripcion}`} />)}</datalist></label>
          <label>{t("doctorConsultation.indications")}<textarea rows={4} maxLength={4000} value={details.indications} onChange={handleDetailsChange("indications")} disabled={isLocked} /></label>
          {!isLocked && (
            <button type="submit" className="button secondary">
              {t("doctorConsultation.saveDetails")}
            </button>
          )}
        </form>
      </section>

      <section className="panel document-panel">
        <h2>{t("doctorConsultation.medicalCertificate")}</h2>
        <p className="muted-text">{t("doctorConsultation.certificateHint")}</p>
        <label>{t("doctorConsultation.certificateReason")}<textarea rows={3} maxLength={2000} value={details.certificateReason} onChange={handleDetailsChange("certificateReason")} disabled={isLocked} /></label>
        <button type="button" className="button secondary" onClick={handleCertificateDownload}>{t("doctorConsultation.downloadCertificate")}</button>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>{t("doctorConsultation.prescriptionTitle")}</h2>
          {isFinalized && (
            <span className="status-badge status-completed">{t("doctorConsultation.finalized")}</span>
          )}
        </div>

        <h3>{t("doctorConsultation.medicines")}</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>{t("doctorConsultation.medicineName")}</th>
              <th>{t("doctorConsultation.dosage")}</th>
              <th>{t("doctorConsultation.frequency")}</th>
              <th>{t("doctorConsultation.duration")}</th>
              <th>{t("doctorConsultation.instructions")}</th>
              {!isFinalized && <th></th>}
            </tr>
          </thead>
          <tbody>
            {medicines.map((m, i) => (
              <tr key={i}>
                <td>
                  <input
                    value={m.medicineName}
                    list="medicine-options"
                    onChange={updateMedicine(i, "medicineName")}
                    disabled={isFinalized}
                    placeholder={t("doctorConsultation.medicineNamePlaceholder")}
                  />
                </td>
                <td>
                  <input value={m.dosage || ""} onChange={updateMedicine(i, "dosage")} disabled={isFinalized} />
                </td>
                <td>
                  <input
                    value={m.frequency || ""}
                    onChange={updateMedicine(i, "frequency")}
                    disabled={isFinalized}
                  />
                </td>
                <td>
                  <input value={m.duration || ""} onChange={updateMedicine(i, "duration")} disabled={isFinalized} />
                </td>
                <td>
                  <input
                    value={m.instructions || ""}
                    onChange={updateMedicine(i, "instructions")}
                    disabled={isFinalized}
                  />
                </td>
                {!isFinalized && (
                  <td>
                    <button type="button" className="link-button" onClick={() => removeMedicineRow(i)}>
                      {t("common.remove")}
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        <datalist id="medicine-options">{medicineCatalogue.medicines.map((medicine) => <option key={medicine.genericName} value={medicine.genericName}>{medicine.commonBrands?.length ? `${medicine.commonBrands.join(", ")} · ${medicine.category}` : medicine.category}</option>)}</datalist>
        {!isFinalized && (
          <button type="button" className="link-button" onClick={addMedicineRow}>
            + {t("doctorConsultation.addMedicine")}
          </button>
        )}

        <h3 style={{ marginTop: 24 }}>{t("doctorConsultation.tests")}</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>{t("doctorConsultation.testName")}</th>
              <th>{t("doctorConsultation.instructions")}</th>
              {!isFinalized && <th></th>}
            </tr>
          </thead>
          <tbody>
            {tests.length === 0 && (
              <tr>
                <td colSpan={3}>{t("doctorConsultation.noTests")}</td>
              </tr>
            )}
            {tests.map((item, i) => (
              <tr key={i}>
                <td>
                  <input
                    value={item.testName}
                    onChange={updateTest(i, "testName")}
                    disabled={isFinalized}
                    placeholder={t("doctorConsultation.testNamePlaceholder")}
                  />
                </td>
                <td>
                  <input
                    value={item.instructions || ""}
                    onChange={updateTest(i, "instructions")}
                    disabled={isFinalized}
                  />
                </td>
                {!isFinalized && (
                  <td>
                    <button type="button" className="link-button" onClick={() => removeTestRow(i)}>
                      {t("common.remove")}
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        {!isFinalized && (
          <button type="button" className="link-button" onClick={addTestRow}>
            + {t("doctorConsultation.addTest")}
          </button>
        )}

        <div className="form" style={{ marginTop: 20 }}>
          <label>
            {t("doctorConsultation.prescriptionNotes")}
            <textarea
              rows={3}
              maxLength={2000}
              value={prescriptionNotes}
              onChange={(e) => setPrescriptionNotes(e.target.value)}
              disabled={isFinalized}
            />
          </label>
        </div>

        <div className="prescription-actions">
          {!isFinalized && (
            <>
              <button type="button" className="button secondary" onClick={handleSaveDraft} disabled={saving}>
                {t("doctorConsultation.saveDraft")}
              </button>
              <button type="button" className="button primary" onClick={handleFinalize} disabled={saving}>
                {t("doctorConsultation.finalizePrescription")}
              </button>
            </>
          )}
          {prescription?.id && (
            <button type="button" className="button secondary" onClick={handleDownload}>
              {t("doctorConsultation.downloadPdf")}
            </button>
          )}
        </div>
      </section>
    </div>
  );
}
