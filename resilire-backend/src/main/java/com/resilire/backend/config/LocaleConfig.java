package com.resilire.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the response language from the request's Accept-Language header
 * (sent by the frontend based on the user's selected/detected language).
 * Chile is the primary market, so Spanish is the default when no header is sent.
 */
@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("es"));
        resolver.setSupportedLocales(List.of(Locale.forLanguageTag("es"), Locale.ENGLISH));
        return resolver;
    }
}
