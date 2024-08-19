package com.netgrif.application.engine.configuration.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;

public class TrailingSlashRedirectFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (path.endsWith("/")) {
            String newPath = path.substring(0, path.length() - 1);
            HttpServletRequest newRequest = new CustomHttpServletRequestWrapper(httpRequest, newPath);
            chain.doFilter(newRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final String newPath;
        public CustomHttpServletRequestWrapper(HttpServletRequest request, String newPath) {
            super(request);
            this.newPath = newPath;
        }

        @Override
        public String getRequestURI() {
            return newPath;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName()).append(":").append(getServerPort())
                    .append(newPath);
            return url;
        }
    }
}
