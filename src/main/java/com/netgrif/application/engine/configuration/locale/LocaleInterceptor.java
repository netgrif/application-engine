package com.netgrif.application.engine.configuration.locale;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.TimeZone;

/**
 * The interceptor that extracts time zone from request headers.
 * It's configuration can be found in {@link com.netgrif.application.engine.configuration.LocaleConfiguration}
 * */
public class LocaleInterceptor implements HandlerInterceptor {

    private static final String TIMEZONE_OFFSET_HEADER_NAME = "X-Timezone-Offset";

    /**
     * The handle function that is being called to extract time zone offset from request header.
     * @param request the request object
     * @param response the response object
     * @param handler the handler object
     * @return boolean whether the chain should be continued with next interceptor
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getHeader(TIMEZONE_OFFSET_HEADER_NAME) == null) {
            return true;
        }
        /*Here retrieve the offset from request headers*/
        String timeZoneOffset = request.getHeader(TIMEZONE_OFFSET_HEADER_NAME);
        /*Retrieve the locale from request headers*/
        Locale locale = request.getLocale();
        /*Convert zoneOffset to TimeZone*/
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(-Integer.parseInt(timeZoneOffset) * 60);
        TimeZone timeZone = TimeZone.getTimeZone(zoneOffset);
        /*Set the locale to requestContextUtils*/
        LocaleContextResolver localeResolver = (LocaleContextResolver) RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocaleContext(request, response, new SimpleTimeZoneAwareLocaleContext(locale, timeZone));
        }
        return true;
    }
}
