package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.configuration.properties.NaeServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Autowired
    private NaeServerProperties serverProperties;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (authException != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("HTTP Status 401 - " + authException.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName(serverProperties.getSecurity().getRealmName());
        super.afterPropertiesSet();
    }
}
