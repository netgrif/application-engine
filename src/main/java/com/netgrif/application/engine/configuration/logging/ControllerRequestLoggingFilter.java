package com.netgrif.application.engine.configuration.logging;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

public class ControllerRequestLoggingFilter extends CommonsRequestLoggingFilter {

    public ControllerRequestLoggingFilter() {
        super.setIncludeQueryString(true);
        super.setIncludeHeaders(true);
        super.setBeforeMessagePrefix("");
        super.setBeforeMessageSuffix("");
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return request.getRequestURI().contains("/api");
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        super.beforeRequest(request, message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
    }
}