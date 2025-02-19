package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.*;
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.application.engine.event.events.user.UserRegistrationEvent;
import com.netgrif.application.engine.orgstructure.groups.config.GroupConfigurationProperties;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.service.interfaces.IRoleService;
import com.netgrif.application.engine.startup.SystemUserRunner;
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;

public class UserService extends AbstractUserService {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthorityRepository authorityRepository;

    @Autowired
    protected IRoleService roleService;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected INextGroupService groupService;

    @Autowired
    protected IRegistrationService registrationService;

    @Autowired
    private GroupConfigurationProperties groupProperties;

    @Autowired
    private IFilterImportExportService filterImportExportService;

    // TODO: release/8.0.0 cleanup, remove boolean params

    @Override
    public IUser saveNewAndAuthenticate(IUser user) {
        return saveNew(user, true);
    }

    @Override
    public IUser saveNew(IUser user) {
        return saveNew(user, false);
    }

    private IUser saveNew(IUser user, boolean ignored) {
        registrationService.encodeUserPassword((RegisteredUser) user);
        addDefaultRole(user);
        addDefaultAuthorities(user);

        User savedUser = userRepository.save((User) user);
        filterImportExportService.createFilterImport(user);
        filterImportExportService.createFilterExport(user);

        if (groupProperties.isDefaultEnabled()) {
            groupService.createGroup(user);
        }
        if (groupProperties.isSystemEnabled()) {
            groupService.addUserToDefaultGroup(user);
        }
        publisher.publishEvent(new UserRegistrationEvent(savedUser));

        return savedUser;
    }

    @Override
    public AnonymousUser saveNewAnonymous(AnonymousUser user) {
        addAnonymousRole(user);
        addAnonymousAuthorities(user);

        return userRepository.save(user);
    }

    @Override
    public User update(IUser user, UpdateUserRequest updates) {
        User dbUser = (User) user;
        if (updates.telNumber != null) {
            dbUser.setTelNumber(updates.telNumber);
        }
        if (updates.avatar != null) {
            dbUser.setAvatar(updates.avatar);
        }
        if (updates.name != null) {
            dbUser.setName(updates.name);
        }
        if (updates.surname != null) {
            dbUser.setSurname(updates.surname);
        }
        dbUser = userRepository.save(dbUser);
        return dbUser;
    }

    public void addDefaultRole(User user) {
        user.addRole(roleService.defaultRole());
    }

    public void addAnonymousRole(User user) {
        user.addRole(roleService.anonymousRole());
    }

    public void addDefaultAuthorities(User user) {
        if (!user.getAuthorities().isEmpty()) {
            return;
        }
        HashSet<Authority> authorities = new HashSet<>();
        authorities.add(authorityRepository.findByName(Authority.user));
        user.setAuthorities(authorities);
    }

    public void addAnonymousAuthorities(User user) {
        if (!user.getAuthorities().isEmpty()) {
            return;
        }
        HashSet<Authority> authorities = new HashSet<>();
        authorities.add(authorityRepository.findByName(Authority.anonymous));
        user.setAuthorities(authorities);
    }

    @Override
    public IUser findByAuth(Authentication auth) {
        return findByEmail(auth.getName());
    }

    @Override
    public IUser save(IUser user) {
        return userRepository.save((User) user);
    }

    @Override
    public IUser findById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Could not find user with id [" + id + "]");
        }
        return user.get();
    }

    @Override
    public boolean existsById(String id) {
        return userRepository.existsById(id);
    }

    @Override
    public IUser resolveById(String id) {
        return findById(id);
    }

    @Override
    public IUser findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public IUser findAnonymousByEmail(String email) {
        return findByEmail(email);
    }


    @Override
    public List<IUser> findAll() {
        return changeType(userRepository.findAll());

    }

    @Override
    public Page<IUser> findAllCoMembers(LoggedUser loggedUser, Pageable pageable) {
        // TODO: 8/27/18 make all pageable
        Set<String> members = groupService.getAllCoMembers(loggedUser.getSelfOrImpersonated().transformToUser());
        members.add(loggedUser.getSelfOrImpersonated().getId());
        Set<ObjectId> objMembers = members.stream().map(ObjectId::new).collect(Collectors.toSet());
        return changeType(userRepository.findAllByIdInAndState(objMembers, UserState.ACTIVE, pageable), pageable);
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, LoggedUser loggedUser, Pageable pageable) {
        Set<String> members = groupService.getAllCoMembers(loggedUser.getSelfOrImpersonated().transformToUser());
        members.add(loggedUser.getSelfOrImpersonated().getId());

        return changeType(userRepository.findAll(buildPredicate(members.stream().map(ObjectId::new)
                .collect(Collectors.toSet()), query), pageable), pageable);

    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, List<ObjectId> roleIds, List<ObjectId> negateRoleIds, LoggedUser loggedUser, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty()))
            return searchAllCoMembers(query, loggedUser, pageable);

        if (negateRoleIds == null) {
            negateRoleIds = new ArrayList<>();
        }


        Set<String> members = groupService.getAllCoMembers(loggedUser.getSelfOrImpersonated().transformToUser());
        members.add(loggedUser.getSelfOrImpersonated().getId());
        BooleanExpression predicate = buildPredicate(members.stream().map(ObjectId::new).collect(Collectors.toSet()), query);
        if (!(roleIds == null || roleIds.isEmpty())) {
            predicate = predicate.and(QUser.user.roles.any().id.in(roleIds));
        }
        predicate = predicate.and(QUser.user.roles.any().id.in(negateRoleIds).not());
        Page<User> users = userRepository.findAll(predicate, pageable);

        return changeType(users, pageable);
    }

    private BooleanExpression buildPredicate(Set<ObjectId> members, String query) {
        BooleanExpression predicate = QUser.user
                .id.in(members)
                .and(QUser.user.state.eq(UserState.ACTIVE));
        for (String word : query.split(" ")) {
            predicate = predicate
                    .andAnyOf(QUser.user.email.containsIgnoreCase(word),
                            QUser.user.name.containsIgnoreCase(word),
                            QUser.user.surname.containsIgnoreCase(word));
        }
        return predicate;
    }

    @Override
    public Page<IUser> findAllActiveByRoles(Set<String> roleIds, Pageable pageable) {
        Page<User> users = userRepository.findDistinctByStateAndRoles_IdIn(UserState.ACTIVE, new ArrayList<>(roleIds), pageable);
        return changeType(users, pageable);
    }

    @Override
    public List<IUser> findAllByRoles(Set<String> roleIds) {
        List<User> users = userRepository.findAllByRoles_IdIn(new ArrayList<>(roleIds));
        return changeType(users);
    }

    @Override
    public List<IUser> findAllByIds(Set<String> ids) {
        List<User> users = userRepository.findAllByIdIn(ids.stream().map(ObjectId::new).collect(Collectors.toSet()));
        return changeType(users);
    }

    @Override
    public IUser assignAuthority(String userId, String authorityId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Authority> authority = authorityRepository.findById(authorityId);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("Could not find user with id [" + userId + "]");
        }
        if (authority.isEmpty()) {
            throw new IllegalArgumentException("Could not find authority with id [" + authorityId + "]");
        }

        user.get().addAuthority(authority.get());
        authority.get().addUser(user.get());

        return userRepository.save(user.get());
    }

    @Override
    public IUser getLoggedOrSystem() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String) {
                return getSystem();
            }
            return getLoggedUser();
        } catch (NullPointerException e) {
            return getSystem();
        }
    }

    @Override
    public IUser getSystem() {
        IUser system = userRepository.findByEmail(SystemUserRunner.SYSTEM_USER_EMAIL);
        system.setRoles(new HashSet<>(roleService.findAll()));
        return system;
    }

    @Override
    public IUser getLoggedUser() {
        LoggedUser loggedUser = getLoggedUserFromContext();
        if (!loggedUser.isAnonymous()) {
            IUser user = findByEmail(loggedUser.getEmail());
            if (loggedUser.isImpersonating()) {
                // cannot be simply reloaded from DB, impersonated user holds a subset of roles and authorities.
                // this reloads the impersonated user's roles as they are not complete (LoggedUser creates incomplete ProcessRole objects)
                IUser impersonated = loggedUser.getImpersonated().transformToUser();
                impersonated.setRoles(roleService.findByIds(loggedUser.getImpersonated().getRoles()));
                user.setImpersonated(impersonated);
            }
            return user;
        }
        return loggedUser.transformToAnonymousUser();
    }

    @Override
    public LoggedUser getAnonymousLogged() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(UserProperties.ANONYMOUS_AUTH_KEY)) {
            return getLoggedUser().transformToLoggedUser();
        }
        return getLoggedUserFromContext();
    }

    @Override
    public LoggedUser getLoggedUserFromContext() {
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

//    @Override
//    public IUser addRole(IUser user, String roleStringId) {
//        ProcessRole role = processRoleService.findById(roleStringId);
//        user.addProcessRole(role);
//        return userRepository.save(user);
//    }
//
//    @Override
//    public IUser removeRole(IUser user, String roleStringId) {
//        ProcessRole role = processRoleService.findByImportId(roleStringId);
//        user.removeProcessRole(role);
//        return userRepository.save(user);
//    }

    @Override
    public void deleteUser(IUser user) {
        User dbUser = (User) user;
        if (userRepository.findById(dbUser.getStringId()).isEmpty()) {
            throw new IllegalArgumentException("Could not find user with id [" + dbUser.getId() + "]");
        }
        userRepository.delete(dbUser);
    }


/*    private User loadProcessRoles(User user) {
        if (user == null)
            return null;
        user.setProcessRoles(processRoleRepository.findAllById(user.getUserProcessRoles()
                .stream().map(UserProcessRole::getRoleId).collect(Collectors.toList())));
        return user;
    }*/

    private User loadGroups(User user) {
        if (user == null)
            return null;
        user.setNextGroups(this.groupService.getAllGroupsOfUser(user));
        return user;
    }
}