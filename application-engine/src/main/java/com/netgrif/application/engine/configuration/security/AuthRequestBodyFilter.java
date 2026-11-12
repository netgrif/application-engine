package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.adapter.spring.configuration.filters.NetgrifOncePerRequestFilter;
import com.netgrif.application.engine.adapter.spring.configuration.filters.requests.NetgrifHttpServletRequest;
import com.netgrif.application.engine.configuration.NaeSecurityConfiguration;
import com.netgrif.application.engine.utils.HttpRequestParamConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

/**
 * This filter extracts the request body (input stream) into a JSON object, so it can be read multiple times. It's
 * saved by {@link NetgrifHttpServletRequest#addAdditionalParameter(String, Object)} under the key
 * {@link HttpRequestParamConstants#REQUEST_BODY}
 * */
@Slf4j
@Component
public class AuthRequestBodyFilter extends NetgrifOncePerRequestFilter {

    public static final String REQUEST_BODY_ATTRIBUTE = AuthRequestBodyFilter.class.getName() + ".body";

    public AuthRequestBodyFilter() {
        setRequestMatcher(Set.of(NaeSecurityConfiguration.loginPathMatcher())); // so the inputStream won't be closed for further processing
    }

    @Override
    protected void doFilterInternal(NetgrifHttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("Reading authentication request body input stream into JSON...");
        if (request.getInputStream() == null) {
            log.debug("Request does not contain a body.");
            filterChain.doFilter(new NetgrifHttpServletRequest(request), response);
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(request.getInputStream());

            request.addAdditionalParameter(HttpRequestParamConstants.REQUEST_BODY, jsonNode);
            request.setAttribute(REQUEST_BODY_ATTRIBUTE, jsonNode);

            log.debug("Request body was successfully read.");
        } catch (Exception e) {
            log.error("Could not read request body: {}", e.getMessage(), e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
