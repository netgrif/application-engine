package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUser;
import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.configuration.filters.NetgrifOncePerRequestFilter;
import com.netgrif.application.engine.adapter.spring.configuration.filters.requests.NetgrifHttpServletRequest;
import com.netgrif.application.engine.auth.service.AnonymousUserRefService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.utils.HttpReqRespUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PublicAuthenticationFilter extends NetgrifOncePerRequestFilter {

    private final AnonymousUserRefService anonymousUserRefService;
    private final AuthorityService authorityService;
    private final RealmService realmService;

    public PublicAuthenticationFilter(AnonymousUserRefService anonymousUserRefService,
                                      AuthorityService authorityService,
                                      RealmService realmService,
                                      SecurityConfigurationProperties securityConfigurationProperties) {
        this.anonymousUserRefService = anonymousUserRefService;
        this.authorityService = authorityService;
        this.realmService = realmService;
        setRequestMatcher(Arrays.stream(securityConfigurationProperties.getServerPatterns()).map(AntPathRequestMatcher::new).collect(Collectors.toSet()));
    }

    @Override
    protected void doFilterInternal(NetgrifHttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        log.trace("PublicAnonymousAuthFilter handling {}", path);

        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.isAuthenticated()) {
            log.debug("Existing authenticated principal: {}", current.getPrincipal());
            filterChain.doFilter(request, response);
            return;
        }

        Realm realm = HttpReqRespUtils.extractRealmFromRequest(request);
        if (realm == null) {
            log.debug("Realm unavailable for public request; skipping anon auth");
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Loaded realm {} (publicAccess={})", realm.getName(), realm.isPublicAccess());
        if (!realm.isPublicAccess()) {
            log.debug("Public access disabled for realm {}; skipping anon auth", realm.getName());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            log.trace("Performing anonymous public auth for realm {}", realm.getName());
            Optional<AnonymousUserRef> refOpt = anonymousUserRefService.getRef(realm.getName());
            if (refOpt.isEmpty()) {
                log.debug("AnonymousUserRef missing for realm {}; skipping anon auth", realm.getName());
                filterChain.doFilter(request, response);
                return;
            }
            AnonymousUserRef ref = refOpt.get();
            Authority anonAuthority = authorityService.getOrCreate(Authority.anonymous);
            log.debug("Using AnonymousUserRef id {} for realm {}", ref.getId(), realm.getName());

            AnonymousUser anonymousUser = new AnonymousUser(ref, anonAuthority);
            LoggedUserImpl userDetails = (LoggedUserImpl) ActorTransformer.toLoggedUser(anonymousUser);
            userDetails.setSessionTimeout(realm.getPublicSessionTimeout());
            log.debug("Created anonymous user details for user: {}", userDetails.getUsername());

            AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(
                    "engine",
                    userDetails,
                    userDetails.getAuthorities()
            );
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(token);
            log.debug("Anonymous public auth succeeded for realm {}", realm.getName());
        } catch (Exception ex) {
            log.debug("Anonymous public auth failed for realm {}: {}", realm.getName(), ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    @Bean
    public FilterRegistrationBean<PublicAuthenticationFilter> publicAnonymousAuthFilterFilterRegistrationBean(PublicAuthenticationFilter filter) {
        FilterRegistrationBean<PublicAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
