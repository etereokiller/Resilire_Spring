import { useTranslation } from "react-i18next";

export default function PrivacyPage() {
  const { t } = useTranslation();
  return (
    <div className="content-page">
      <h1>{t("privacy.title")}</h1>
      <p>{t("privacy.paragraph1")}</p>
      <p>{t("privacy.paragraph2")}</p>
    </div>
  );
}
