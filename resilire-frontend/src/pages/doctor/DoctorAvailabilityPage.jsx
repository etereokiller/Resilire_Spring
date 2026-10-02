import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as doctorApi from "../../api/doctorApi";

const DAYS = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

export default function DoctorAvailabilityPage() {
  const { t } = useTranslation();
  const [slots, setSlots] = useState([]);
  const [form, setForm] = useState({ dayOfWeek: "MONDAY", startTime: "09:00", endTime: "13:00", slotDurationMinutes: "30" });
  const [error, setError] = useState("");

  const loadSlots = () => {
    doctorApi
      .listMyAvailability()
      .then(setSlots)
      .catch(() => setError(t("doctorAvailability.errorFallbackLoad")));
  };

  useEffect(() => {
    loadSlots();
  }, []);

  const handleChange = (field) => (event) =>
    setForm((prev) => ({ ...prev, [field]: event.target.value }));

  const handleAdd = async (event) => {
    event.preventDefault();
    setError("");
    const overlaps = slots.some((slot) => slot.dayOfWeek === form.dayOfWeek && form.startTime < slot.endTime.slice(0, 5) && slot.startTime.slice(0, 5) < form.endTime);
    if (overlaps) {
      setError(t("doctorAvailability.overlapError"));
      return;
    }
    try {
      await doctorApi.addAvailability({
        dayOfWeek: form.dayOfWeek,
        startTime: `${form.startTime}:00`,
        endTime: `${form.endTime}:00`,
        slotDurationMinutes: Number(form.slotDurationMinutes === "custom" ? form.customDuration : form.slotDurationMinutes),
      });
      loadSlots();
    } catch (err) {
      setError(err.response?.data?.message || t("doctorAvailability.errorFallbackAdd"));
    }
  };

  const handleDelete = async (id) => {
    setError("");
    try {
      await doctorApi.deleteAvailability(id);
      loadSlots();
    } catch (err) {
      setError(err.response?.data?.message || t("doctorAvailability.errorFallbackRemove"));
    }
  };

  return (
    <div className="form-page">
      <h1>{t("doctorAvailability.title")}</h1>
      {error && <p className="form-error">{error}</p>}

      <form onSubmit={handleAdd} className="form inline-form">
        <label>
          {t("doctorAvailability.day")}
          <select value={form.dayOfWeek} onChange={handleChange("dayOfWeek")}>
            {DAYS.map((day) => (
              <option key={day} value={day}>
                {t(`common.days.${day}`)}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t("doctorAvailability.start")}
          <input type="time" value={form.startTime} onChange={handleChange("startTime")} />
        </label>
        <label>
          {t("doctorAvailability.end")}
          <input type="time" value={form.endTime} onChange={handleChange("endTime")} />
        </label>
        <label>
          {t("doctorAvailability.duration")}
          <select value={form.slotDurationMinutes} onChange={handleChange("slotDurationMinutes")}>
            <option value="30">30 {t("doctorAvailability.minutes")}</option>
            <option value="45">45 {t("doctorAvailability.minutes")}</option>
            <option value="60">60 {t("doctorAvailability.minutes")}</option>
            <option value="custom">{t("doctorAvailability.customDuration")}</option>
          </select>
        </label>
        {form.slotDurationMinutes === "custom" && <label>{t("doctorAvailability.customDuration")}<input type="number" min="5" max="480" step="5" value={form.customDuration || ""} onChange={handleChange("customDuration")} /></label>}
        <button type="submit" className="button primary">
          {t("doctorAvailability.addSlot")}
        </button>
      </form>

      <table className="data-table">
        <thead>
          <tr>
            <th>{t("doctorAvailability.day")}</th>
            <th>{t("doctorAvailability.start")}</th>
            <th>{t("doctorAvailability.end")}</th>
            <th>{t("doctorAvailability.duration")}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {slots.length === 0 && (
            <tr>
              <td colSpan={5}>{t("doctorAvailability.noSlots")}</td>
            </tr>
          )}
          {slots.map((slot) => (
            <tr key={slot.id}>
              <td>{t(`common.days.${slot.dayOfWeek}`)}</td>
              <td>{slot.startTime}</td>
              <td>{slot.endTime}</td>
              <td>{slot.slotDurationMinutes} {t("doctorAvailability.minutes")}</td>
              <td>
                <button className="link-button" onClick={() => handleDelete(slot.id)}>
                  {t("doctorAvailability.remove")}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
