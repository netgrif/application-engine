package com.netgrif.application.engine.ldap.filters;

import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import com.netgrif.application.engine.configuration.security.AuthenticationService;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;


public class LoginAttemptsFilter extends GenericFilterBean {


    private final String[] PERMIT_ALL_STATIC_PATTERNS = {
            "/bower_components/", "/scripts/", "/assets/", "/styles/", "/views/", "/favicon.ico", "/manifest.json", "/configuration/", "/swagger-resources/", "/swagger-ui.html", "/webjars/",
            "/index.html", "/login", "/signup/", "/recover/", "/api/auth/signup", "/api/auth/token/verify", "/api/auth/reset", "/api/auth/recover", "/v2/api-docs", "/swagger-ui.html"
    };


    @Override

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (((HttpServletRequest) request).getRequestURI().equalsIgnoreCase("/") || isPermitted(((HttpServletRequest) request).getRequestURI()))
            chain.doFilter(request, response);
        else {
            AuthenticationService service = (AuthenticationService) ApplicationContextProvider.getBean("authenticationService");
            String clientIP = AuthenticationService.getClientIP((HttpServletRequest) request);
            if (service.isIPBlocked(clientIP)) {
                ((HttpServletResponse) response).sendError(401, "Account is temporary blocked due to exceeding the number of unsuccessful login attempts. Please wait a moment and then try again.");
                return;
            }
            chain.doFilter(request, response);
        }
    }


    private boolean isPermitted(String url) {
        return Arrays.stream(PERMIT_ALL_STATIC_PATTERNS).anyMatch(url::startsWith);
    }
}