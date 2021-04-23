package com.netgrif.workflow.configuration.security;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.configuration.security.jwt.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Slf4j
public class PublicAuthenticationFilter extends OncePerRequestFilter {

    private final ProviderManager authenticationManager;
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final Authority anonymousRole;

    private final static String JWT_HEADER_NAME = "X-Jwt-Token";
    private final static String BEARER = "Bearer ";
    private final String[] anonymousAccessUrls;

    private final IJwtService jwtService;
    private final IUserService userService;

    public PublicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                      Authority anonymousRole, String[] urls, IJwtService jwtService,
                                      IUserService userService) {
        this.authenticationManager = authenticationManager;
        this.authenticationManager.getProviders().add(provider);
        this.anonymousRole = anonymousRole;
        this.anonymousAccessUrls = urls;
        this.jwtService = jwtService;
        this.userService = userService;
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

    private void authenticate(HttpServletRequest request, String jwtToken){
        AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken(
                UserProperties.ANONYMOUS_AUTH_KEY,
                jwtService.getLoggedUser(jwtToken, this.anonymousRole),
                Collections.singleton(this.anonymousRole)
        );
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    private String resolveValidToken(HttpServletRequest request, HttpServletResponse response) {
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

        try {
            jwtService.isExpired(jwtToken);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
            resolveClaims(claims, request);
            jwtToken = jwtService.tokenFrom(claims);
        }

        return jwtToken;
    }

    private void resolveClaims(Map<String, Object> claims, HttpServletRequest request) {
        LoggedUser loggedUser = createAnonymousUser(request);

        if (claims.containsKey("user")) {
            User user = userService.findByEmail((String)((LinkedHashMap)claims.get("user")).get("email"), false);
            if (user != null)
                loggedUser = user.transformToLoggedUser();
        }

        claims.put("user", loggedUser);
        claims.put("authorities", this.anonymousRole);
    }

    private LoggedUser createAnonymousUser(HttpServletRequest request) {
        String hash = new ObjectId().toString();

        AnonymousUser anonymousUser = (AnonymousUser) this.userService.findByEmail(hash + "@nae.com", false);

        if (anonymousUser == null) {
            anonymousUser = new AnonymousUser(hash + "@anonymous.nae",
                    "",
                    "User",
                    "Anonymous"
            );
            anonymousUser.setState(UserState.ACTIVE);
            userService.saveNewAnonymous(anonymousUser);
        }
        return anonymousUser.transformToLoggedUser();
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
