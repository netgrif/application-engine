package com.netgrif.application.engine.adapter.spring.configuration.filters;

import com.netgrif.application.engine.adapter.spring.configuration.filters.requests.NetgrifHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Proxy filter class to cast request object to {@link NetgrifHttpServletRequest} for further use
 * */
@Setter
@Slf4j
public abstract class  NetgrifOncePerRequestFilter extends OncePerRequestFilter {

    /**
     * If initialized, the filter will be applied only if the request path is matched. Otherwise, it will just continue to the next filter.
     * */
    protected Set<RequestMatcher> requestMatcher;

    protected abstract void doFilterInternal(NetgrifHttpServletRequest request, HttpServletResponse response,
                                             FilterChain filterChain) throws ServletException, IOException;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (requestNotMatches(request)) {
            log.trace("Request did not match the required URIs: {}", this.requestMatcher);
            filterChain.doFilter(request, response);
            return;
        }

        NetgrifHttpServletRequest typedRequest = (NetgrifHttpServletRequest) request;
        doFilterInternal(typedRequest, response, filterChain);
    }

    protected boolean requestNotMatches(@NotNull HttpServletRequest request) {
        if (requestMatcher == null) {
            return false;
        }
        boolean result = false;
        for (RequestMatcher matcher : requestMatcher) {
            result |= matcher.matches(request);
        }
        return !result;
    }
}
