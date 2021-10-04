package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.AbstractUserService;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties;
import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.domain.QOAuthUser;
import com.netgrif.workflow.oauth.domain.RemoteGroupResource;
import com.netgrif.workflow.oauth.domain.RemoteUserResource;
import com.netgrif.workflow.oauth.domain.repositories.OAuthUserRepository;
import com.netgrif.workflow.oauth.service.interfaces.IOAuthUserService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteGroupResourceService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import com.netgrif.workflow.startup.SystemUserRunner;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;

public class OAuthUserService extends AbstractUserService implements IOAuthUserService {

    public static final Logger log = LoggerFactory.getLogger(OAuthUserService.class);

    @Autowired
    protected NaeOAuthProperties oAuthProperties;

    @Autowired
    protected OAuthUserRepository repository;

    @Autowired
    protected UserRepository userRepository;

    protected IRemoteUserResourceService<RemoteUserResource> remoteUserResourceService;
    protected IRemoteGroupResourceService<RemoteGroupResource, RemoteUserResource> remoteGroupResourceService;

    public OAuthUserService(IRemoteUserResourceService<RemoteUserResource> remoteUserResourceService,
                            IRemoteGroupResourceService<RemoteGroupResource, RemoteUserResource> remoteGroupResourceService) {
        this.remoteUserResourceService = remoteUserResourceService;
        this.remoteGroupResourceService = remoteGroupResourceService;
    }

    @Override
    public OAuthUser findByOAuthId(String id) {
        return repository.findByOauthId(id);
    }

    @Override
    public OAuthUser findByUsername(String username) {
        RemoteUserResource res = remoteUserResourceService.findUserByUsername(username);
        return res != null ? resolveFromDbOrProvideRepresentation(res) : null;
    }

    @Override
    public IUser findByAuth(Authentication auth) {
        return findById(((LoggedUser) auth.getPrincipal()).getId(), false);
    }

    @Override
    public IUser save(IUser user) {
        return repository.save((OAuthUser) user);
    }

    @Override
    public IUser saveNewAndAuthenticate(IUser user) {
        return saveNew(user);
    }

    @Override
    public IUser saveNew(IUser user) {
        addDefaultRole(user);
        addDefaultAuthorities(user);
        user.setState(UserState.ACTIVE);
        user = save(user);
        return user;
    }

    @Override
    public AnonymousUser saveNewAnonymous(AnonymousUser user) {
        addDefaultRole(user);
        addDefaultAuthorities(user);

        return userRepository.save(user);
    }

    @Override
    public IUser update(IUser user, UpdateUserRequest updates) {
        throw new UnsupportedOperationException("User cannot be updated in this user base");
    }

    @Override
    public IUser findById(String id, boolean small) {
        Optional<User> dbUser = userRepository.findById(id);
        if (dbUser.isPresent()) return dbUser.get();

        RemoteUserResource res = getResourceById(id);
        return resolveFromDbOrProvideRepresentation(res);
    }

    @Override
    public IUser resolveById(String id, boolean small) {
        Optional<User> dbUser = userRepository.findById(id);
        if (dbUser.isPresent()) return dbUser.get();

        RemoteUserResource userRepresentation = getResourceById(id);
        OAuthUser user = findByOAuthId(id);
        if (user == null) {
            user = createNewUser(id);
        }
        loadUser(user, userRepresentation, small);
        return user;
    }

    @Override
    public IUser findByEmail(String email, boolean small) {
        User dbUser = userRepository.findByEmail(email);
        if (dbUser != null) return dbUser;

        RemoteUserResource res = remoteUserResourceService.findUserByEmail(email);
        return res != null ? resolveFromDbOrProvideRepresentation(res) : null;
    }

    @Override
    public IUser findAnonymousByEmail(String email, boolean small) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<IUser> findAll(boolean small) {
        return remoteUserResourceService.listUsers(Pageable.unpaged()).stream()
                .map(this::fromUserRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public Page<IUser> findAllCoMembers(LoggedUser loggedUser, boolean small, Pageable pageable) {
        Page<RemoteUserResource> page = remoteUserResourceService.listUsers(pageable);
        return new PageImpl<>(
                page.getContent().stream().map(this::resolveFromDbOrProvideRepresentation).collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    @Override
    public Page<IUser> findAllActiveByProcessRoles(Set<String> roleIds, boolean small, Pageable pageable) {
        Page<OAuthUser> users = repository.findDistinctByStateAndProcessRoles__idIn(UserState.ACTIVE, new ArrayList<>(roleIds), pageable);
        return changeType(users, pageable);
    }

    @Override
    public List<IUser> findAllByIds(Set<String> ids, boolean small) {
        List<RemoteUserResource> users = remoteUserResourceService.findUsers(ids);
        return users.stream().map(this::resolveFromDbOrProvideRepresentation).collect(Collectors.toList());
    }

    @Override
    public List<IUser> findAllByProcessRoles(Set<String> roleIds, boolean small) {
        List<OAuthUser> users = repository.findAllByProcessRoles__idIn(new ArrayList<>(roleIds));
        return changeType(users);
    }

    @Override
    public IUser getLoggedOrSystem() {
        try {
            return getLoggedUser();
        } catch (NullPointerException e) {
            return getSystem();
        }
    }

    @Override
    public IUser getSystem() {
        return userRepository.findByEmail(SystemUserRunner.SYSTEM_USER_EMAIL);
    }

    @Override
    public IUser getLoggedUser() {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!loggedUser.isAnonymous()) {
            return findById(loggedUser.getId(), false);
        }
        return loggedUser.transformToAnonymousUser();
    }

    @Override
    public void deleteUser(IUser user) {
        repository.delete((OAuthUser) user);
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, LoggedUser principal, Boolean small, Pageable pageable) {
        Page<RemoteUserResource> page = remoteUserResourceService.searchUsers(query, pageable, small);
        return new PageImpl<>(
                page.getContent().stream().map(this::resolveFromDbOrProvideRepresentation).collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, List<ObjectId> roleIds, List<ObjectId> negateRoleIds, LoggedUser loggedUser, Boolean small, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty())) {
            return searchAllCoMembers(query, loggedUser, small, pageable);
        }
        Page<RemoteUserResource> page = remoteUserResourceService.searchUsers(query, pageable, small);
        BooleanExpression predicate = QOAuthUser.oAuthUser.oauthId.in(page.getContent().stream().map(RemoteUserResource::getId).collect(Collectors.toList()));
        if (roleIds != null && !roleIds.isEmpty()) {
            predicate = predicate.and(QOAuthUser.oAuthUser.processRoles.any()._id.in(negateRoleIds != null ? negateRoleIds : new ArrayList<>()).not());
            predicate = predicate.and(QOAuthUser.oAuthUser.processRoles.any()._id.in(roleIds));
            List<OAuthUser> users = (List<OAuthUser>) repository.findAll(predicate);
            Map<String, RemoteUserResource> map = page.getContent().stream().collect(Collectors.toMap(RemoteUserResource::getId, it -> it));
            return new PageImpl<>(users.stream()
                    .map(it -> {
                        try {
                            loadUser(it, map.get(it.getOauthId()), true);
                        } catch (IllegalArgumentException e) {
                            log.error("Failed to load user by id " + it.getOauthId(), e);
                            return null;
                        }
                        return it;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList()), pageable, page.getTotalElements());
        } else {
            predicate = predicate.and(QOAuthUser.oAuthUser.processRoles.any()._id.in(negateRoleIds != null ? negateRoleIds : new ArrayList<>()));
            List<OAuthUser> users = (List<OAuthUser>) repository.findAll(predicate);
            Map<String, OAuthUser> map = users.stream().collect(Collectors.toMap(OAuthUser::getStringId, it -> it));
            return new PageImpl<>(page.getContent().stream()
                    .filter(it -> !map.containsKey(it.getId()))
                    .map(this::resolveFromDbOrProvideRepresentation)
                    .collect(Collectors.toList()), pageable, page.getTotalElements());
        }
    }

    protected OAuthUser resolveFromDbOrProvideRepresentation(RemoteUserResource resource) {
        OAuthUser oAuthUser = repository.findByOauthId(resource.getId());
        if (oAuthUser == null) {
            oAuthUser = fromUserRepresentation(resource);
        } else {
            loadUser(oAuthUser, resource);
        }
        return oAuthUser;
    }

    protected OAuthUser fromUserRepresentation(RemoteUserResource representation) {
        if (representation == null)
            return null;

        OAuthUser oAuthUser = new OAuthUser();
        oAuthUser.setOauthId(representation.getId());
        loadUser(oAuthUser, representation);
        return oAuthUser;
    }

    protected void loadUser(OAuthUser user, boolean small) {
        RemoteUserResource userRepresentation = getResourceById(user.getOauthId());
        loadUser(user, userRepresentation, small);
    }

    protected void loadUser(OAuthUser user, RemoteUserResource resource, boolean small) {
        loadUser(user, resource);
        if (!small) {
            user.setRemoteGroups(remoteGroupResourceService.groupsOfUser(user.getOauthId()));
        }
    }

    protected void loadUser(OAuthUser user, RemoteUserResource resource) {
        user.setName(resource.getFirstName());
        user.setSurname(resource.getLastName());
        user.setEmail(resource.getEmail());
    }

    protected OAuthUser createNewUser(String id) {
        OAuthUser user = new OAuthUser();
        user.setOauthId(id);
        user.setState(UserState.ACTIVE);
        user = (OAuthUser) saveNew(user);
        return user;
    }

    protected RemoteUserResource getResourceById(String id) {
        RemoteUserResource resource = remoteUserResourceService.findUser(id);
        if (resource == null)
            throw new IllegalArgumentException("Could not find user with");

        return resource;
    }
}
