package com.netgrif.workflow.configuration.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.configuration.security.jwt.JwtFactory;
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
import java.util.Date;

@Slf4j
public class PublicAuthenticationFilter extends OncePerRequestFilter {

    private final ProviderManager authenticationManager;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private Authority anonymousRole;


    public PublicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider, Authority anonymousRole) {
        this.authenticationManager = authenticationManager;
        this.authenticationManager.getProviders().add(provider);
        this.anonymousRole = anonymousRole;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getServletPath())) {
            log.info("Trying to authenticate anonymous user...");

            AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken("anonymousUser", request.getRemoteAddr(), Collections.singleton(this.anonymousRole));
            authRequest.setDetails(
                    this.authenticationDetailsSource.buildDetails(request));
            Authentication authResult = this.authenticationManager
                    .authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authResult);

            createAnonymousJwtToken(request, response);
            log.info("Anonymous user was successfully authenticated.");
        }
        filterChain.doFilter(request, response);

    }

    private void createAnonymousJwtToken(HttpServletRequest request, HttpServletResponse response) {
        String bearerToken = "Bearer " + JwtFactory.tokenFrom(request.getRemoteAddr());
        response.setHeader("jwtToken", bearerToken);
    }


    private boolean isPublicApi(String path) {
        return path.startsWith("/api/public");
    }
}
