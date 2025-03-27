package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public abstract class PublicJwtAuthenticationFilter extends PublicAuthenticationFilter {
    protected final static String JWT_HEADER_NAME = "X-Jwt-Token";
    protected final static String BEARER = "Bearer ";

    protected final IJwtService jwtService;

    public PublicJwtAuthenticationFilter(IIdentityService identityService, IRoleService roleService, ProviderManager authenticationManager,
                                         AnonymousAuthenticationProvider provider, ApplicationRole anonymousAppRole,
                                         ProcessRole anonymousProcessRole, String[] urls, String[] exceptions,
                                         IJwtService jwtService) {
        super(identityService, roleService, authenticationManager, provider, anonymousAppRole, anonymousProcessRole, urls, exceptions);
        this.jwtService = jwtService;
    }

    /**
     * todo javadoc
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            String jwtToken = resolveValidToken(request);
            authenticate(request, jwtService.getLoggedIdentity(jwtToken));
            response.setHeader(JWT_HEADER_NAME, BEARER + jwtToken);
            log.info("Anonymous identity was authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    /**
     * todo javadoc
     */
    protected String resolveValidToken(HttpServletRequest request) {
        Map<String, Object> claims = new HashMap<>();
        String jwtHeader = request.getHeader(JWT_HEADER_NAME);
        String jwtToken;

        if (jwtHeader == null || !jwtHeader.startsWith(BEARER)) {
            log.warn("There is no JWT token or token is invalid.");
            resolveClaims(claims);
            jwtToken = jwtService.tokenFrom(claims);
        } else {
            jwtToken = jwtHeader.replace(BEARER, "");
        }

        try {
            jwtService.isExpired(jwtToken);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
            resolveClaims(claims);
            jwtToken = jwtService.tokenFrom(claims);
        }

        return jwtToken;
    }

    /**
     * todo javadoc
     */
    protected void resolveClaims(Map<String, Object> claims) {
        Optional<Identity> identityOpt = Optional.empty();

        if (claims.containsKey("identity")) {
            identityOpt = identityService.findByUsername((String) ((LinkedHashMap<?, ?>) claims.get("identity")).get("username"));
        }

        Identity identity = identityOpt.orElseGet(this::createAnonymousIdentityWithActor);

        LoggedIdentity loggedIdentity = identity.toSession();
        loggedIdentity.eraseCredentials();

        claims.put("identity", loggedIdentity);
    }
}
