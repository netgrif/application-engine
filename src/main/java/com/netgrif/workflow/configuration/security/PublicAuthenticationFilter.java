package com.netgrif.workflow.configuration.security;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.configuration.security.jwt.JwtUtils;
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
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private Authority anonymousRole;
    private final static String JWT_HEADER_NAME = "Jwt-Auth-Token";
    private final static String BEARER = "Bearer ";



    public PublicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider, Authority anonymousRole) {
        this.authenticationManager = authenticationManager;
        this.authenticationManager.getProviders().add(provider);
        this.anonymousRole = anonymousRole;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            log.info("Trying to authenticate anonymous user...");
            String jwtToken = resolveValidToken(request);
            authenticate(request, jwtToken);
            response.setHeader(JWT_HEADER_NAME, jwtToken);
            log.info("Anonymous user was successfully authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String jwtToken){
        AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken(
                "anonymousUser",
                JwtUtils.getLoggedUser(jwtToken),
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
            jwtToken = JwtUtils.tokenFrom(claims);
        } else {
            jwtToken = jwtHeader.replace(BEARER, "");
        }

        if (JwtUtils.isExpired(jwtToken)) {
            log.warn("Jwt token for [" + JwtUtils.getClaim(jwtToken, "address", String.class) + "] is expired.");
            resolveClaims(claims, request);
            jwtToken = JwtUtils.tokenFrom(claims);
        }

        return jwtToken;
    }

    private void resolveClaims(Map<String, Object> claims, HttpServletRequest request) {
        LoggedUser user = new LoggedUser(-1L,
                request.getRemoteAddr(),
                "",
                Collections.singleton(this.anonymousRole)
        );
        claims.put("user", user);
        claims.put("authorities", this.anonymousRole);
    }

    private boolean isPublicApi(String path) {
        return path.startsWith("/api/public");
    }
}
