package com.netgrif.workflow.oauth

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
@Disabled("UnsatisfiedDependency Error creating bean with name")
class KeycloakAdminTest {

    @Value('${security.oauth2.client.realm}')
    String realm

    @Autowired
    protected Keycloak keycloak

    @Test
    void test() {
        RealmResource realmResource = keycloak.realm(realm)
        UsersResource usersResource = realmResource.users()

        List<UserRepresentation> users = usersResource.list()
        assert users != null

    }
}
