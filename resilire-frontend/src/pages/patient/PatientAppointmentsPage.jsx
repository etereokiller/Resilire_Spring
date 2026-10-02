import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as appointmentApi from "../../api/appointmentApi";

export default function PatientAppointmentsPage() {
  const { t } = useTranslation();
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");
  const [showCompleted, setShowCompleted] = useState(false);

  const loadAppointments = () => {
    appointmentApi
      .listMyAppointmentsAsPatient()
      .then(setAppointments)
      .catch(() => setError(t("patientAppointments.errorFallbackLoad")));
  };

  useEffect(() => {
    loadAppointments();
  }, []);

  const handleCancel = async (id) => {
    if (!window.confirm(t("patientAppointments.confirmCancel"))) {
      return;
    }
    setError("");
    try {
      await appointmentApi.cancelAppointmentAsPatient(id);
      loadAppointments();
    } catch (err) {
      setError(err.response?.data?.message || t("patientAppointments.errorFallbackCancel"));
    }
  };

  return (
    <div className="form-page wide">
      <h1>{t("patientAppointments.title")}</h1>
      <button className="button secondary" onClick={() => setShowCompleted(!showCompleted)}>{showCompleted ? t("patientAppointments.showActive") : t("patientAppointments.showCompleted")}</button>
      {error && <p className="form-error">{error}</p>}
      <table className="data-table">
        <thead>
          <tr>
            <th>{t("patientAppointments.doctor")}</th>
            <th>{t("patientAppointments.doctorRut")}</th>
            <th>{t("patientAppointments.specialization")}</th>
            <th>{t("patientAppointments.date")}</th>
            <th>{t("patientAppointments.time")}</th>
            <th>{t("patientAppointments.status")}</th>
            <th>{t("patientAppointments.consultation")}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {appointments.filter((appt) => showCompleted ? appt.status === "COMPLETED" : appt.status !== "COMPLETED").length === 0 && (
            <tr>
              <td colSpan={8}>{t("patientAppointments.noAppointments")}</td>
            </tr>
          )}
          {appointments.filter((appt) => showCompleted ? appt.status === "COMPLETED" : appt.status !== "COMPLETED").map((appt) => (
            <tr key={appt.id}>
              <td>
                {t("common.doctorTitle")} {appt.doctorName}
              </td>
              <td>{appt.doctorRut}</td>
              <td>{appt.doctorSpecialization}</td>
              <td>{appt.appointmentDate}</td>
              <td>
                {appt.startTime} - {appt.endTime}
              </td>
              <td>{t(`common.status.${appt.status}`)}</td>
              <td>
                {appt.status === "SCHEDULED" && (
                  appt.meetingLink && appt.joinable ? (
                    <a
                      className="link-button"
                      href={appt.meetingLink}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {t("patientAppointments.joinConsultation")}
                    </a>
                  ) : (
                    <span title={t("patientAppointments.linkOpensLater")}>
                      <button type="button" className="button secondary" disabled>{t("patientAppointments.joinOnline")}</button>
                    </span>
                  )
                )}
              </td>
              <td>
                {appt.status !== "CANCELLED" && (
                  <Link className="link-button" to={`/patient/appointments/${appt.id}/consultation`}>
                    {t("patientAppointments.consultationRecord")}
                  </Link>
                )}
                {appt.status === "SCHEDULED" && (
                  <>
                    {" "}
                    <button className="link-button" onClick={() => handleCancel(appt.id)}>
                      {t("patientAppointments.cancel")}
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
