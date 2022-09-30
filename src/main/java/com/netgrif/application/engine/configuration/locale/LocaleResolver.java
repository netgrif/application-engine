package com.netgrif.application.engine.configuration.locale;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * A custom implementation of locale resolver to store the time zone offset in
 * request context
 * */
public class LocaleResolver implements LocaleContextResolver {
    private final LocaleContextResolver localeContextResolver;
    private final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver;

    public LocaleResolver(LocaleContextResolver localeContextResolver) {
        this.localeContextResolver = localeContextResolver;
        acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setDefaultLocale(Locale.getDefault());
    }

    @Override
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        return localeContextResolver.resolveLocaleContext(request);
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
        localeContextResolver.setLocaleContext(request, response, localeContext);

    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return acceptHeaderLocaleResolver.resolveLocale(request);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        acceptHeaderLocaleResolver.setLocale(request, response, locale);

    }
}
