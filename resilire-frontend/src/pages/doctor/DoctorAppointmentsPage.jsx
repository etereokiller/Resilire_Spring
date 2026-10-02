import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as appointmentApi from "../../api/appointmentApi";

export default function DoctorAppointmentsPage() {
  const { t } = useTranslation();
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");
  const [showCompleted, setShowCompleted] = useState(false);

  const loadAppointments = () => {
    appointmentApi
      .listMyAppointmentsAsDoctor()
      .then(setAppointments)
      .catch(() => setError(t("doctorAppointments.errorFallbackLoad")));
  };

  useEffect(() => {
    loadAppointments();
  }, []);

  const handleCancel = async (id) => {
    if (!window.confirm(t("doctorAppointments.confirmCancel"))) {
      return;
    }
    setError("");
    try {
      await appointmentApi.cancelAppointmentAsDoctor(id);
      loadAppointments();
    } catch (err) {
      setError(err.response?.data?.message || t("doctorAppointments.errorFallbackCancel"));
    }
  };

  const handleComplete = async (id) => {
    setError("");
    try {
      await appointmentApi.completeAppointmentAsDoctor(id);
      loadAppointments();
    } catch (err) {
      setError(err.response?.data?.message || t("doctorAppointments.errorFallbackComplete"));
    }
  };

  const visibleAppointments = appointments.filter((appt) =>
    showCompleted
      ? ["COMPLETED", "CANCELLED"].includes(appt.status)
      : !["COMPLETED", "CANCELLED"].includes(appt.status)
  );

  return (
    <div className="form-page wide">
      <h1>{t("doctorAppointments.title")}</h1>
      <button className="button secondary" onClick={() => setShowCompleted(!showCompleted)}>{showCompleted ? t("doctorAppointments.showActive") : t("doctorAppointments.showCompleted")}</button>
      {error && <p className="form-error">{error}</p>}
      <table className="data-table">
        <thead>
          <tr>
            <th>{t("doctorAppointments.patient")}</th>
            <th>{t("doctorAppointments.patientRut")}</th>
            <th>{t("doctorAppointments.date")}</th>
            <th>{t("doctorAppointments.time")}</th>
            <th>{t("doctorAppointments.status")}</th>
            <th>{t("doctorAppointments.consultation")}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {visibleAppointments.length === 0 && (
            <tr>
              <td colSpan={7}>{t("doctorAppointments.noAppointments")}</td>
            </tr>
          )}
          {visibleAppointments.map((appt) => (
            <tr key={appt.id}>
              <td>{appt.patientName}</td>
              <td>{appt.patientRut}</td>
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
                      {t("doctorAppointments.joinConsultation")}
                    </a>
                  ) : (
                    <span title={t("doctorAppointments.linkOpensLater")}>
                      <button type="button" className="button secondary" disabled>{t("doctorAppointments.joinOnline")}</button>
                    </span>
                  )
                )}
              </td>
              <td>
                {appt.status !== "CANCELLED" && (
                  <Link className="link-button" to={`/doctor/appointments/${appt.id}/consultation`}>
                    {t("doctorAppointments.consultationRecord")}
                  </Link>
                )}
                {appt.status === "SCHEDULED" && (
                  <>
                    {" "}
                    <button className="link-button" onClick={() => handleComplete(appt.id)}>
                      {t("doctorAppointments.markCompleted")}
                    </button>{" "}
                    <button className="link-button" onClick={() => handleCancel(appt.id)}>
                      {t("doctorAppointments.cancel")}
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
