package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * todo javadoc
 */
@Slf4j
public class PublicBasicAuthenticationFilter extends PublicJwtAuthenticationFilter  {

    public PublicBasicAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                           Authority anonymousAuthority, String[] urls, String[] exceptions, IJwtService jwtService) {
        super(authenticationManager, provider, anonymousAuthority, urls, exceptions, jwtService);
    }

    /**
     * todo javadoc
     */
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
                IdentityProperties.ANONYMOUS_AUTH_KEY,
                jwtService.getLoggedUser(jwtToken, this.anonymousAuthority),
                Collections.singleton(this.anonymousAuthority)
        );
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    /**
     * todo javadoc
     * @return
     */
    @Override
    protected Identity createAnonymousIdentityWithActor() {
        return null;
    }

    private Identity createAnonymousUser(HttpServletRequest request) {
        String hash = new ObjectId().toString();

        // TODO: release/8.0.0 string constants, properties?
        AnonymousUser anonymousUser = (AnonymousUser) this.userService.findAnonymousByEmail(hash + "@nae.com");

        if (anonymousUser == null) {
            anonymousUser = new AnonymousUser(hash + "@anonymous.nae",
                    "n/a",
                    "User",
                    "Anonymous"
            );
            anonymousUser.setState(IdentityState.ACTIVE);
            userService.saveNewAnonymous(anonymousUser);
        }
        return anonymousUser.transformToLoggedUser();
    }
}
