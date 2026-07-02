package com.netgrif.application.engine.configuration.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActuatorRequestFilterTest {

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsNonActuatorRequestsWithoutAuthentication() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request("/api/tasks"), response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void allowsHealthEndpointWithoutAuthentication() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request("/manage/health"), response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsUnauthenticatedActuatorRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request("/manage/metrics"), response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals(
                "HTTP Status 401 - Full authentication is required to access this resource",
                response.getContentAsString()
        );
    }

    @Test
    void rejectsAnonymousActuatorRequest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request("/manage/metrics"), response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void rejectsActuatorRequestWithoutAdminAuthority() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                List.of(new SimpleGrantedAuthority("USER"))
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request("/manage/metrics"), response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertEquals("HTTP Status 403 - Forbidden", response.getContentAsString());
    }

    @Test
    void allowsActuatorRequestForAdminAuthority() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin",
                "password",
                List.of(new SimpleGrantedAuthority("ADMIN"))
        ));
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request("/manage/metrics"), response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    private ActuatorRequestFilter filter() {
        WebEndpointProperties properties = new WebEndpointProperties();
        properties.setBasePath("/manage");
        return new ActuatorRequestFilter(properties);
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        return request;
    }
}
