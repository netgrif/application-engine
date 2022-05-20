package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.configuration.security.wrapper.HeaderRequestWrapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CredentialsConverterFilter extends OncePerRequestFilter {

    @Getter
    @Setter
    private Charset credentialsCharset = StandardCharsets.UTF_8;

    private static final String AUTHENTICATION_SCHEME_BASIC = "Basic";

    public CredentialsConverterFilter() {}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HeaderRequestWrapper wrappedRequest = new HeaderRequestWrapper(request);
        convert(wrappedRequest);
        filterChain.doFilter(wrappedRequest, response);
    }

    private void convert(HeaderRequestWrapper request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return;
        }
        if (!StringUtils.startsWithIgnoreCase(header, AUTHENTICATION_SCHEME_BASIC)) {
            return ;
        }
        if (header.equalsIgnoreCase(AUTHENTICATION_SCHEME_BASIC)) {
            throw new BadCredentialsException("Empty basic authentication token");
        }

        header = header.trim();
        byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
        byte[] decoded = Base64.getDecoder().decode(base64Token);
        String token = URLDecoder.decode(new String(decoded, this.credentialsCharset), this.credentialsCharset);

        int delim = token.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }

        String urlDecodedHeader = AUTHENTICATION_SCHEME_BASIC + " " + new String(Base64.getEncoder().encode(token.getBytes()));
        request.addHeader(HttpHeaders.AUTHORIZATION, urlDecodedHeader);
    }
}
