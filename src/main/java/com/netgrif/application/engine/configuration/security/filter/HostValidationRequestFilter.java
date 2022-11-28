package com.netgrif.application.engine.configuration.security.filter;

import com.netgrif.application.engine.configuration.properties.SecurityConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class HostValidationRequestFilter extends OncePerRequestFilter {

    protected SecurityConfigProperties properties;

    public HostValidationRequestFilter(SecurityConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.properties.getHeaders() != null && this.properties.getHeaders().getHostAllowed() != null && !(this.properties.getHeaders().getHostAllowed().isEmpty())) {
            if (this.properties.getHeaders().getHostAllowed().stream()
                    .noneMatch(request.getHeader("Host")::equalsIgnoreCase)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP 400 Bad Request - Client sends invalid HTTP Request: Host header value");
            }
        }
        filterChain.doFilter(request, response);
    }
}
