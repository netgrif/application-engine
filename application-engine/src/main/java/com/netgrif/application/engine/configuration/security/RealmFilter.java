package com.netgrif.application.engine.configuration.security;

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
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Optional;

/**
 * This filter extracts a realm from the request. It's saved by {@link NetgrifHttpServletRequest#addAdditionalParameter(String, Object)}
 * under the key {@link HttpRequestParamConstants#REALM}. Realm can be null if none is found.
 * */
@Slf4j
@Component
public class RealmFilter extends NetgrifOncePerRequestFilter {

    private static final String REALM_ID_HEADER = "X-Realm-ID";

    private static final String REALM_ID_BODY = "realmId";

    private static final String REALM_NAME_BODY = "realName";

    private static final String REALM_ALIAS_BODY = "realm";

    private final RealmService realmService;

    public RealmFilter(RealmService realmService) {
        this.realmService = realmService;
    }

    @Override
    protected void doFilterInternal(NetgrifHttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.debug("Trying to get realm id from the HTTP request...");

        RealmSelection selection = findRequestedRealm(request);
        Optional<Realm> realmOpt;
        if (selection.specified()) {
            realmOpt = selection.realm();
            if (realmOpt.isEmpty()) {
                log.warn("Authentication rejected because the explicitly requested realm does not exist");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid realm");
                return;
            }
            log.debug("Realm was successfully found in the request");
        } else {
            log.debug("Realm could not be found in the request. Using the default realm if available.");
            realmOpt = realmService.getDefaultRealm();
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

    private RealmSelection findRequestedRealm(NetgrifHttpServletRequest request) {
        String realmId = request.getHeader(REALM_ID_HEADER);
        if (realmId != null && !realmId.isBlank()) {
            return new RealmSelection(true, realmService.getRealmById(realmId));
        }

        tools.jackson.databind.JsonNode requestBodyJson = HttpReqRespUtils.extractBodyFromRequest(request);
        if (requestBodyJson == null) {
            return new RealmSelection(false, Optional.empty());
        }

        tools.jackson.databind.JsonNode realmIdNode = requestBodyJson.get(REALM_ID_BODY);
        if (hasText(realmIdNode)) {
            return new RealmSelection(true, realmService.getRealmById(realmIdNode.textValue()));
        }

        tools.jackson.databind.JsonNode realmNameNode = requestBodyJson.get(REALM_NAME_BODY);
        if (hasText(realmNameNode)) {
            return new RealmSelection(true, realmService.getRealmByName(realmNameNode.textValue()));
        }

        tools.jackson.databind.JsonNode realmAliasNode = requestBodyJson.get(REALM_ALIAS_BODY);
        if (hasText(realmAliasNode)) {
            Optional<Realm> realm = realmService.getRealmById(realmAliasNode.textValue())
                    .or(() -> realmService.getRealmByName(realmAliasNode.textValue()));
            return new RealmSelection(true, realm);
        }

        return new RealmSelection(false, Optional.empty());
    }

    private static boolean hasText(JsonNode node) {
        return node != null && node.textValue() != null && !node.textValue().isBlank();
    }

    private record RealmSelection(boolean specified, Optional<Realm> realm) {}
}