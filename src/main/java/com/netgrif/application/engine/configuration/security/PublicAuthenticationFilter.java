package com.netgrif.application.engine.configuration.security;

import com.netgrif.adapter.auth.domain.AuthorityImpl;
import com.netgrif.core.auth.domain.*;
import com.netgrif.auth.service.AuthorityService;
import com.netgrif.auth.service.UserService;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.core.auth.domain.enums.UserState;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class PublicAuthenticationFilter extends OncePerRequestFilter {

    private final static String JWT_HEADER_NAME = "X-Jwt-Token";
    private final static String BEARER = "Bearer ";
    private final static String USER = "user";
    private final ProviderManager authenticationManager;
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final String[] anonymousAccessUrls;
    private final String[] exceptions;

    private final IJwtService jwtService;
    private final UserService userService;
    private final AuthorityService authorityService;

    public PublicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                      String[] urls, String[] exceptions, IJwtService jwtService,
                                      UserService userService, AuthorityService authorityService) {
        this.authenticationManager = authenticationManager;
        this.authenticationManager.getProviders().add(provider);
        this.anonymousAccessUrls = urls;
        this.exceptions = exceptions;
        this.jwtService = jwtService;
        this.userService = userService;
        this.authorityService = authorityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            String jwtToken = resolveValidToken(request, response);
            authenticate(request, jwtToken);
            response.setHeader(JWT_HEADER_NAME, BEARER + jwtToken);
            log.info("Anonymous user was authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String jwtToken) {
        AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken(
                "anonymous-user",
                jwtService.getLoggedUser(jwtToken, Authority.anonymous),
                Collections.singleton((AuthorityImpl) authorityService.getOrCreate(Authority.anonymous))
        );
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    private String resolveValidToken(HttpServletRequest request, HttpServletResponse response) {
        String jwtHeader = request.getHeader(JWT_HEADER_NAME);
        String jwtToken = null;

        if (jwtHeader == null || !jwtHeader.startsWith(BEARER)) {
            log.warn("There is no JWT token or token is invalid.");
            LoggedUser loggedUser = resolveLoggedUser(jwtToken);
            jwtToken = jwtService.tokenFrom(Collections.emptyMap(), loggedUser.getUsername(), Map.of(USER, loggedUser));
        } else {
            jwtToken = jwtHeader.replace(BEARER, "");
        }

        if (jwtService.isTokenExpired(jwtToken)) {
            LoggedUser loggedUser = resolveLoggedUser(jwtToken);
            jwtToken = jwtService.tokenFrom(Collections.emptyMap(), loggedUser.getUsername(), Map.of(USER, loggedUser));
        }
        return jwtToken;
    }

    private LoggedUser resolveLoggedUser(String existingToken) {
        LoggedUser loggedUser;
        if (existingToken != null) {
            loggedUser = jwtService.getLoggedUser(existingToken, Authority.anonymous);
        } else {
            loggedUser = createAnonymousUser();
        }
        //MODULARISATION: check if its okay this way
//        loggedUser.eraseCredentials();
        return loggedUser;
    }

    private LoggedUser createAnonymousUser() {
        String hash = new ObjectId().toString();
        User anonymousUser = new com.netgrif.adapter.auth.domain.User();
        anonymousUser.setState(UserState.ACTIVE);
        anonymousUser = (User) userService.saveUser(anonymousUser, null);
        return (LoggedUser) userService.transformToLoggedUser(anonymousUser);
    }

    private boolean isPublicApi(String path) {
        for (String url : this.anonymousAccessUrls) {
            if (path.matches(url.replace("*", ".*?"))) {
                for (String ex : this.exceptions) {
                    if (path.matches(ex.replace("*", ".*?"))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
