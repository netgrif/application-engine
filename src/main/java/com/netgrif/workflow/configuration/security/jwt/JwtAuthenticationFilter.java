package com.netgrif.workflow.configuration.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class JwtAuthenticationFilter extends AnonymousAuthenticationFilter {

    public static final String SECRET = "SECRET_KEY";
    public static final long EXPIRATION_TIME = 900000; // 15 mins
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Anonymous";
    public static final String SIGN_UP_URL = "/api/services/controller/user";

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(String key) {
        super(key);
    }

    @Override
    protected Authentication createAuthentication(HttpServletRequest request) {
        return super.createAuthentication(request);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        super.doFilter(req, res, chain);
        String header = ((HttpServletRequest)req).getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        AnonymousAuthenticationToken authentication = getAuthentication((HttpServletRequest) req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private AnonymousAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();

            if (user != null) {
                return new AnonymousAuthenticationToken(user, "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
            }
            return null;
        }
        return null;
    }

}
