package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnExpression("${nae.oauth.keycloak}")
public class KeycloakUserResourceService implements IRemoteUserResourceService<UserRepresentation> {

    @Value("${security.oauth2.client.realm}")
    protected String realm;

    @Autowired
    protected Keycloak keycloak;

    @Override
    public Page<UserRepresentation> listUsers(Pageable pageable) {
        return page(
                usersResource().list((int) pageable.getOffset(), pageable.getPageSize()),
                pageable,
                countUsers()
        );
    }

    @Override
    public Page<UserRepresentation> searchUsers(String searchString, Pageable pageable, boolean small) {
        return page(
                usersResource().search(searchString, (int) pageable.getOffset(), pageable.getPageSize()),
                pageable,
                countUsers()
        );
    }

    @Override
    public long countUsers() {
        return usersResource().count();
    }

    @Override
    public long countUsers(String searchString) {
        return usersResource().count(searchString);
    }

    @Override
    public UserRepresentation findUserByUsername(String username) {
        List<UserRepresentation> found = usersResource().search(username, true);
        return found.get(0);
    }

    @Override
    public UserRepresentation findUser(String id) {
        return usersResource().get(id).toRepresentation();
    }

    protected UsersResource usersResource() {
        RealmResource realmResource = keycloak.realm(realm);
        return realmResource.users();
    }

    protected Page<UserRepresentation> page(List<UserRepresentation> list, Pageable pageable, long total) {
        return new PageImpl<>(list, pageable, total);
    }

}
