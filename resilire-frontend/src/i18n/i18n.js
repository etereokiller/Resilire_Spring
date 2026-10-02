import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import en from "./en.json";
import es from "./es.json";

export const LANGUAGE_STORAGE_KEY = "resilire_language";
export const SUPPORTED_LANGUAGES = ["en", "es"];

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      es: { translation: es },
    },
    // Default for Chile when the browser sends no usable language signal.
    fallbackLng: "es",
    supportedLngs: SUPPORTED_LANGUAGES,
    load: "languageOnly",
    detection: {
      // A language the user explicitly picked (stored in localStorage) always
      // wins over the browser's auto-detected language on future visits.
      order: ["localStorage", "navigator"],
      lookupLocalStorage: LANGUAGE_STORAGE_KEY,
      caches: ["localStorage"],
    },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
