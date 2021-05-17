package com.netgrif.workflow.configuration;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.oauth.service.interfaces.IOauthUserMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2AuthenticationConvertingFilter extends OncePerRequestFilter {

    private IOauthUserMapper mapper;

    public OAuth2AuthenticationConvertingFilter(IOauthUserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2Authentication)) {
            filterChain.doFilter(request, response);
            return;
        }
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
        LoggedUser loggedUser = mapper.transform(oAuth2Authentication);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUser, "n/a", loggedUser.getAuthorities()));
        filterChain.doFilter(request, response);
    }


}
