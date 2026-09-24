package com.netgrif.application.engine.configuration.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.netgrif.application.engine.adapter.spring.configuration.filters.NetgrifOncePerRequestFilter;
import com.netgrif.application.engine.adapter.spring.configuration.filters.requests.NetgrifHttpServletRequest;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.utils.HttpReqRespUtils;
import com.netgrif.application.engine.utils.HttpRequestParamConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * This filter extracts a realm from the request. It's saved by {@link NetgrifHttpServletRequest#addAdditionalParameter(String, Object)}
 * under the key {@link HttpRequestParamConstants#REALM}. Realm can be null if none is found.
 * */
@Slf4j
@Component
public class RealmFilter extends NetgrifOncePerRequestFilter {

    private final String REALM_ID_HEADER = "X-Realm-ID";
    private final String REALM_ID_BODY = "realmId";
    private final String REALM_NAME_BODY = "realName";

    private final RealmService realmService;

    public RealmFilter(RealmService realmService) {
        this.realmService = realmService;
    }

    @Override
    protected void doFilterInternal(NetgrifHttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("Trying to get realm id from the HTTP request...");

        Optional<Realm> realmOpt = findRealmInHeaders(request);

        if (realmOpt.isEmpty()) {
            realmOpt = findRealmInBody(request);
        }

        if (realmOpt.isEmpty()) {
            log.debug("Realm could not be found in the request. Using the default realm if available.");
            realmOpt = realmService.getDefaultRealm();
        } else {
            log.debug("Realm was successfully found in the request");
        }

        if (realmOpt.isEmpty()) {
            log.debug("No realm could be found. Continuing without realm.");
            filterChain.doFilter(request, response);
            return;
        }

        log.trace("Selected realm: [{}]", realmOpt.get().getName());

        request.addAdditionalParameter(HttpRequestParamConstants.REALM, realmOpt.get());

        filterChain.doFilter(request, response);
    }

    @Bean
    public FilterRegistrationBean<RealmFilter> realmFilterFilterRegistrationBean(RealmFilter filter) {
        FilterRegistrationBean<RealmFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Finds realm by realm id in headers by {@link #REALM_ID_HEADER}
     * */
    private Optional<Realm> findRealmInHeaders(HttpServletRequest request) {
        String realmId = request.getHeader(REALM_ID_HEADER);
        if (realmId == null || realmId.isBlank()) {
            return Optional.empty();
        }
        return realmService.getRealmById(realmId);
    }

    /**
     * Finds realm by realm id {@link #REALM_ID_BODY} or realm name {@link #REALM_NAME_BODY} in request body
     * */
    private Optional<Realm> findRealmInBody(NetgrifHttpServletRequest request) {
        JsonNode requestBodyJson = HttpReqRespUtils.extractBodyFromRequest(request);
        if (requestBodyJson == null) {
            return Optional.empty();
        }

        JsonNode realmIdNode = requestBodyJson.get(REALM_ID_BODY);
        Optional<Realm> realmOpt = Optional.empty();

        if (isJsonNodeValueEmpty(realmIdNode)) {
            realmOpt = realmService.getRealmById(String.valueOf(realmIdNode.textValue()));
        }

        if (realmOpt.isEmpty()) {
            JsonNode realmNameNode = requestBodyJson.get(REALM_NAME_BODY);
            if (isJsonNodeValueEmpty(realmNameNode)) {
                realmOpt = realmService.getRealmByName(realmNameNode.textValue());
            }
        }

        return realmOpt;
    }

    private static boolean isJsonNodeValueEmpty(JsonNode node) {
        return node != null && node.textValue() != null && !node.textValue().isBlank();
    }
}