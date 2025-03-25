package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * todo javadoc
 */
public class PublicAdvancedAuthenticationFilter extends PublicJwtAuthenticationFilter {

    public PublicAdvancedAuthenticationFilter(ProviderManager authenticationManager, AnonymousAuthenticationProvider provider,
                                              Authority anonymousAuthority, String[] urls, String[] exceptions, IJwtService jwtService) {
        super(authenticationManager, provider, anonymousAuthority, urls, exceptions, jwtService);
    }

    /**
     * todo javadoc
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // isPublicApi
            // resolve jwt token
            // authenticate
        filterChain.doFilter(request, response);
    }

    /**
     * todo javadoc
     * @return
     */
    @Override
    protected Identity createAnonymousIdentityWithActor() {
        return null;
    }
}
