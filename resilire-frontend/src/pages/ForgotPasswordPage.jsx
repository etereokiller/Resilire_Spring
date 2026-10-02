
import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as authApi from "../api/authApi";

export default function ForgotPasswordPage() {
  const { t } = useTranslation();
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");
  const submit = async (event) => {
    event.preventDefault(); setError("");
    try { await authApi.requestPasswordReset(email); setSubmitted(true); }
    catch (err) { setError(err.response?.data?.message || t("passwordRecovery.errorFallback")); }
  };
  return <div className="form-page"><h1>{t("passwordRecovery.title")}</h1><p>{t("passwordRecovery.intro")}</p>
    {submitted ? <p className="form-success">{t("passwordRecovery.sent")}</p> : <form className="form" onSubmit={submit}><label>{t("login.email")}<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>{error && <p className="form-error">{error}</p>}<button className="button primary">{t("passwordRecovery.submit")}</button></form>}
    <p><Link to="/login">{t("passwordRecovery.backToLogin")}</Link></p></div>;
}
