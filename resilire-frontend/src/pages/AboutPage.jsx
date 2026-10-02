import { useTranslation } from "react-i18next";

export default function AboutPage() {
  const { t } = useTranslation();
  return (
    <div className="content-page">
      <h1>{t("about.title")}</h1>
      <p>{t("about.paragraph1")}</p>
      <p>{t("about.paragraph2")}</p>
    </div>
  );
}
