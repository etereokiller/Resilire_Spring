import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function NotFoundPage() {
  const { t } = useTranslation();
  return (
    <div className="content-page centered">
      <h1>{t("notFound.title")}</h1>
      <p>{t("notFound.message")}</p>
      <Link to="/" className="button primary">
        {t("notFound.backHome")}
      </Link>
    </div>
  );
}
