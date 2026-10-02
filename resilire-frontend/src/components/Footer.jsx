import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import ResiLogo from "./ResiLogo";

export default function Footer() {
  const { t } = useTranslation();
  return (
    <footer className="footer">
      <div className="footer-inner">
        <ResiLogo width={90} />
        <span>{t("footer.rights", { year: new Date().getFullYear() })}</span>
        <div className="footer-links">
          <Link to="/privacy">{t("footer.privacy")}</Link>
          <Link to="/help">{t("footer.help")}</Link>
          <Link to="/about">{t("footer.about")}</Link>
        </div>
      </div>
    </footer>
  );
}
