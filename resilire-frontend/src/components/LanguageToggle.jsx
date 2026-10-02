import { useTranslation } from "react-i18next";
import { SUPPORTED_LANGUAGES } from "../i18n/i18n";

const LABELS = { en: "EN", es: "ES" };

export default function LanguageToggle() {
  const { i18n } = useTranslation();
  const current = i18n.language;

  return (
    <div className="language-toggle" role="group" aria-label="Language selector">
      {SUPPORTED_LANGUAGES.map((lng) => (
        <button
          key={lng}
          type="button"
          className={`language-toggle-option${current === lng ? " active" : ""}`}
          onClick={() => i18n.changeLanguage(lng)}
          aria-pressed={current === lng}
        >
          {LABELS[lng]}
        </button>
      ))}
    </div>
  );
}
