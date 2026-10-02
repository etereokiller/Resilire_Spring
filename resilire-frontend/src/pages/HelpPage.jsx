import { useTranslation } from "react-i18next";

export default function HelpPage() {
  const { t } = useTranslation();
  return (
    <div className="content-page">
      <h1>{t("help.title")}</h1>
      <p>{t("help.intro")}</p>
      <h2>{t("help.faqTitle")}</h2>
      <ul>
        <li>{t("help.faq1")}</li>
        <li>{t("help.faq2")}</li>
        <li>{t("help.faq3")}</li>
      </ul>
      <p>{t("help.contact")}</p>
    </div>
  );
}
