package com.netgrif.workflow.configuration.security;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.configuration.security.jwt.IJwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PublicAuthenticationFilter extends OncePerRequestFilter {

    private final ProviderManager authenticationManager;
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final Authority anonymousRole;
    private final static String JWT_HEADER_NAME = "X-Jwt-Token";
    private final static String BEARER = "Bearer ";
    private final String[] anonymousAccessUrls;

    private final IJwtService jwtService;

    public PublicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider, Authority anonymousRole, String[] urls, IJwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.authenticationManager.getProviders().add(provider);
        this.anonymousRole = anonymousRole;
        this.anonymousAccessUrls = urls;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            log.info("Trying to authenticate anonymous user...");
            String jwtToken = resolveValidToken(request);
            authenticate(request, jwtToken);
            response.setHeader(JWT_HEADER_NAME, BEARER + jwtToken);
            log.info("Anonymous user was successfully authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String jwtToken){
        AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken(
                "anonymousUser",
                jwtService.getLoggedUser(jwtToken, this.anonymousRole),
                Collections.singleton(this.anonymousRole)
        );
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    private String resolveValidToken(HttpServletRequest request) {
        Map<String, Object> claims = new HashMap<>();
        String jwtHeader = request.getHeader(JWT_HEADER_NAME);
        String jwtToken;

        if (jwtHeader == null || !jwtHeader.startsWith(BEARER)) {
            log.warn("There is no JWT token or token is invalid.");
            resolveClaims(claims, request);
            jwtToken = jwtService.tokenFrom(claims);
        } else {
            jwtToken = jwtHeader.replace(BEARER, "");
        }

        if (jwtService.isExpired(jwtToken)) {
            resolveClaims(claims, request);
            jwtToken = jwtService.tokenFrom(claims);
        }

        return jwtToken;
    }

    private void resolveClaims(Map<String, Object> claims, HttpServletRequest request) {
        claims.put("user", createAnonymousUser(request));
        claims.put("authorities", this.anonymousRole);
    }

    private LoggedUser createAnonymousUser(HttpServletRequest request) {
        long hash = UUID.fromString(request.getSession().getId()).getMostSignificantBits() & Long.MAX_VALUE;
        LoggedUser user = new LoggedUser(hash,
                request.getRemoteAddr(),
                "",
                Collections.singleton(this.anonymousRole)
        );
        user.setFullName("Anonymous " + user.getId().toString());
        user.setAnonymous(true);
        return user;
    }

    private boolean isPublicApi(String path) {
        for (String url : this.anonymousAccessUrls) {
            if (path.matches(url.replace("*", ".*?"))) {
                return true;
            }
        }
        return false;
    }
}
