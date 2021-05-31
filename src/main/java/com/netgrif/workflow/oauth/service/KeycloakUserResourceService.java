package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.oauth.domain.KeycloakUserResource;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakUserResourceService implements IRemoteUserResourceService<KeycloakUserResource> {

    public static final Logger log = LoggerFactory.getLogger(KeycloakUserResourceService.class);

    @Value("${security.oauth2.client.realm}")
    protected String realm;

    @Autowired
    protected Keycloak keycloak;

    @Override
    public Page<KeycloakUserResource> listUsers(Pageable pageable) {
        return page(
                usersResource().list((int) pageable.getOffset(), pageable.getPageSize()),
                pageable,
                countUsers()
        );
    }

    @Override
    public Page<KeycloakUserResource> searchUsers(String searchString, Pageable pageable, boolean small) {
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
    public KeycloakUserResource findUserByUsername(String username) {
        List<UserRepresentation> found = usersResource().search(username, true);
        return getFirstOrNull(found);
    }

    @Override
    public KeycloakUserResource findUser(String id) {
        UserRepresentation resource = getUser(id);
        return resource != null ? wrap(resource) : null;
    }

    @Override
    public List<KeycloakUserResource> findUsers(Set<String> ids) {
        return ids.stream().map(this::findUser).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public KeycloakUserResource findUserByEmail(String email) {
        List<UserRepresentation> found = usersResource().search(null, null, null, email, 0, 1, true, true);
        return getFirstOrNull(found);
    }

    protected UserRepresentation getUser(String id) {
        try {
            return usersResource().get(id).toRepresentation();
        } catch (NotFoundException e) {
            return null;
        }
    }

    protected KeycloakUserResource getFirstOrNull(List<UserRepresentation> found) {
        return found.size() > 0 ? wrap(found.get(0)) : null;
    }

    public UsersResource usersResource() {
        RealmResource realmResource = keycloak.realm(realm);
        return realmResource.users();
    }

    protected Page<KeycloakUserResource> page(List<UserRepresentation> list, Pageable pageable, long total) {
        return new PageImpl<>(list.stream().map(this::wrap).collect(Collectors.toList()), pageable, total);
    }

    protected KeycloakUserResource wrap(UserRepresentation representation) {
        return new KeycloakUserResource(representation);
    }

}
