import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

export default function LandingPage() {
  const { t } = useTranslation();
  return (
    <div>
      <div className="landing-banner">
        <img src="/landing-hero-banner.png" alt="Resilire medical professional" />
      </div>
      <section className="hero">
        <span className="eyebrow">Care that fits your life</span>
        <h1>{t("landing.heroTitle")}</h1>
        <p>{t("landing.heroSubtitle")}</p>
        <div className="hero-actions">
          <Link to="/register/patient" className="button primary">
            {t("landing.iAmPatient")}
          </Link>
          <Link to="/register/doctor" className="button secondary">
            {t("landing.iAmDoctor")}
          </Link>
        </div>
        <div className="hero-trust" aria-label="Resilire benefits">
          <span>Verified specialists</span>
          <span>Secure consultations</span>
          <span>Care on your schedule</span>
        </div>
      </section>

      <section className="home-section">
        <div className="section-intro">
          <h2>Everything you need for better care</h2>
          <p>From finding the right clinician to reviewing your care plan, Resilire keeps every step simple and connected.</p>
        </div>
        <div className="feature-grid">
        <div className="feature-card">
          <div className="feature-icon" aria-hidden="true">⌕</div>
          <h3>{t("landing.feature1Title")}</h3>
          <p>{t("landing.feature1Body")}</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon" aria-hidden="true">◷</div>
          <h3>{t("landing.feature2Title")}</h3>
          <p>{t("landing.feature2Body")}</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon" aria-hidden="true">✚</div>
          <h3>{t("landing.feature3Title")}</h3>
          <p>{t("landing.feature3Body")}</p>
        </div>
        </div>
      </section>
    </div>
  );
}
