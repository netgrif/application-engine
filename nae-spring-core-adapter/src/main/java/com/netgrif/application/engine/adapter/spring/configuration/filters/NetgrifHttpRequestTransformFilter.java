package com.netgrif.application.engine.adapter.spring.configuration.filters;


import com.netgrif.application.engine.adapter.spring.configuration.filters.requests.NetgrifHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This filter wraps the {@link HttpServletRequest} object into {@link NetgrifHttpServletRequest} object
 * */
@Slf4j
@Component
public class NetgrifHttpRequestTransformFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest transformedRequest = new NetgrifHttpServletRequest(request);
        log.debug("Http request was transformed to Netgrif http request wrapper");
        filterChain.doFilter(transformedRequest, response);
    }

    @Bean
    public FilterRegistrationBean<NetgrifHttpRequestTransformFilter> netgrifHttpRequestTransformFilterFilterRegistrationBean(NetgrifHttpRequestTransformFilter filter) {
        FilterRegistrationBean<NetgrifHttpRequestTransformFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
