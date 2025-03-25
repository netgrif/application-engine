package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public abstract class PublicJwtAuthenticationFilter extends PublicAuthenticationFilter {
    protected final static String JWT_HEADER_NAME = "X-Jwt-Token";
    protected final static String BEARER = "Bearer ";

    protected final IJwtService jwtService;

    public PublicJwtAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                         Authority anonymousAuthority, String[] urls, String[] exceptions, IJwtService jwtService) {
        super(authenticationManager, provider, anonymousAuthority, urls, exceptions);
        this.jwtService = jwtService;
    }

    protected String resolveValidToken(HttpServletRequest request, HttpServletResponse response) {
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

    protected void resolveClaims(Map<String, Object> claims, HttpServletRequest request) {
        Identity identity = createAnonymousIdentityWithActor();

        if (claims.containsKey("user")) {
            IUser user = userService.findAnonymousByEmail((String) ((LinkedHashMap) claims.get("user")).get("email"));
            if (user != null) {
                identity = user.transformToLoggedUser();
            }
        }
        identity.eraseCredentials();
        claims.put("user", identity);
    }
}
