package com.netgrif.application.engine.configuration.authentication.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.ldap.embedded.base-dn=dc=netgrif,dc=com",
        "spring.ldap.embedded.credential.username=cn=admin,dc=netgrif,dc=com",
        "spring.ldap.embedded.credential.password=secret",
        "spring.ldap.embedded.ldif=file:src/test/resources/test-server.ldif",
        "spring.ldap.embedded.port=6389",
        "nae.security.providers=NetgrifBasicAuthenticationProvider,NetgrifLdapAuthenticationProvider",
        "spring.ldap.embedded.validation.enabled=false",
        "nae.ldap.enabled=true",
        "nae.ldap.url=ldap://localhost:6389",
        "nae.ldap.username=cn=admin,dc=netgrif,dc=com",
        "nae.ldap.password=secret",
        "nae.ldap.base=dc=netgrif,dc=com",
        "nae.ldap.userFilter=cn={0}",
        "nae.ldap.peopleSearchBase=ou=people",
        "nae.ldap.groupSearchBase=ou=groups",
        "nae.ldap.peopleClass=inetOrgPerson,person"})
class NetgrifLdapAuthenticationProviderTest {

    @Autowired
    private WebApplicationContext wac;

    private static final String USER_EMAIL = "ben@netgrif.com";
    private static final String USER_PASSW = "benpassword";

    @BeforeEach
    public void before() {

    }

    @Test
    void testLogin() throws Exception {
        MockMvc mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSW);
        user.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()));

        mvc.perform(get("/api/user/me")
                        .with(authentication(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
    }


}
