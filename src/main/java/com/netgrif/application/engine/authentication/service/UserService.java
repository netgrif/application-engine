package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.*;
import com.netgrif.application.engine.authentication.domain.repositories.UserRepository;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.web.requestbodies.UpdateUserRequest;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleAssignmentService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.event.events.user.UserRegistrationEvent;
import com.netgrif.application.engine.orgstructure.groups.config.GroupConfigurationProperties;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.startup.SystemIdentityRunner;
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
    protected IRoleAssignmentService roleAssignmentService;

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

    // TODO: release/8.0.0 cleanup

    @Override
    public IUser saveNewAndAuthenticate(IUser user) {
        return saveNew(user);
    }

    @Override
    public IUser saveNew(IUser user) {
        registrationService.encodePassword((RegisteredUser) user);
        addDefaultAuthorities(user);

        User savedUser = userRepository.save((User) user);
        filterImportExportService.createFilterImport(user);
        filterImportExportService.createFilterExport(user);

        // todo 2058
//        if (groupProperties.isDefaultEnabled()) {
//            groupService.createGroup(user);
//        }
//        if (groupProperties.isSystemEnabled()) {
//            groupService.addUserToDefaultGroup(user);
//        }
        publisher.publishEvent(new UserRegistrationEvent(savedUser));

        return savedUser;
    }

    @Override
    public AnonymousUser saveNewAnonymous(AnonymousUser user) {
        addAnonymousAuthorities(user);
        user = userRepository.save(user);

        Role anonymousRole = roleService.findAnonymousRole();
        roleService.assignRolesToActor(user.getStringId(), Set.of(anonymousRole.getStringId()));

        return user;
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

    public void addDefaultAuthorities(User user) {
        if (!user.getAuthorities().isEmpty()) {
            return;
        }
        HashSet<SessionRole> authorities = new HashSet<>();
        authorities.add(authorityRepository.findByName(SessionRole.user));
        user.setAuthorities(authorities);
    }

    public void addAnonymousAuthorities(User user) {
        if (!user.getAuthorities().isEmpty()) {
            return;
        }
        HashSet<SessionRole> authorities = new HashSet<>();
        authorities.add(authorityRepository.findByName(SessionRole.anonymous));
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
    public Page<IUser> findAllCoMembers(Identity identity, Pageable pageable) {
        // TODO: 8/27/18 make all pageable
        Set<String> members = groupService.getAllCoMembers(identity.getSelfOrImpersonated().transformToUser());
        members.add(identity.getSelfOrImpersonated().getId());
        Set<ObjectId> objMembers = members.stream().map(ObjectId::new).collect(Collectors.toSet());
        return changeType(userRepository.findAllByIdInAndState(objMembers, IdentityState.ACTIVE, pageable), pageable);
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, Identity identity, Pageable pageable) {
        Set<String> members = groupService.getAllCoMembers(identity.getSelfOrImpersonated().transformToUser());
        members.add(identity.getSelfOrImpersonated().getId());

        return changeType(userRepository.findAll(buildPredicate(members.stream().map(ObjectId::new)
                .collect(Collectors.toSet()), query), pageable), pageable);

    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, List<ObjectId> roleIds, List<ObjectId> negateRoleIds, Identity identity, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty()))
            return searchAllCoMembers(query, identity, pageable);

        if (negateRoleIds == null) {
            negateRoleIds = new ArrayList<>();
        }


        Set<String> members = groupService.getAllCoMembers(identity.getSelfOrImpersonated().transformToUser());
        members.add(identity.getSelfOrImpersonated().getId());
        BooleanExpression predicate = buildPredicate(members.stream().map(ObjectId::new).collect(Collectors.toSet()), query);
        if (!(roleIds == null || roleIds.isEmpty())) {
            // todo 2058
//            predicate = predicate.and(QUser.user.roles.any().id.in(roleIds));
        }
//        predicate = predicate.and(QUser.user.roles.any().id.in(negateRoleIds).not());
        Page<User> users = userRepository.findAll(predicate, pageable);

        return changeType(users, pageable);
    }

    private BooleanExpression buildPredicate(Set<ObjectId> members, String query) {
        BooleanExpression predicate = QUser.user
                .id.in(members)
                .and(QUser.user.state.eq(IdentityState.ACTIVE));
        for (String word : query.split(" ")) {
            predicate = predicate
                    .andAnyOf(QUser.user.email.containsIgnoreCase(word),
                            QUser.user.name.containsIgnoreCase(word),
                            QUser.user.surname.containsIgnoreCase(word));
        }
        return predicate;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Page<IUser> findAllActiveByRoles(Set<String> roleIds, Pageable pageable) {
        List<RoleAssignment> assignments = roleAssignmentService.findAllByRoleIdIn(roleIds);
        Set<ObjectId> userIds = assignments.stream()
                .map(assignment -> new ObjectId(assignment.getActorId()))
                .collect(Collectors.toSet());
        Page<User> users = userRepository.findAllByIdIn(userIds, pageable);
        return changeType(users, pageable);
    }

    @Override
    public List<IUser> findAllByIds(Set<String> ids) {
        List<User> users = userRepository.findAllByIdIn(ids.stream().map(ObjectId::new).collect(Collectors.toSet()));
        return changeType(users);
    }

    @Override
    public IUser assignAuthority(String userId, String authorityId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<SessionRole> authority = authorityRepository.findById(authorityId);

        if (user.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find user with id [%s]", userId));
        }
        if (authority.isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find authority with id [%s]", authorityId));
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
        IUser system = userRepository.findByEmail(SystemIdentityRunner.SYSTEM_IDENTITY_EMAIL);
        List<ProcessRole> roles = roleService.findAllProcessRoles();
        Set<String> roleIds = roles.stream().map(ProcessRole::getStringId).collect(Collectors.toSet());
        roleService.assignRolesToActor(system.getStringId(), roleIds);
        return system;
    }

    @Override
    public IUser getLoggedUser() {
        Identity identity = getLoggedUserFromContext();
        if (!identity.isAnonymous()) {
            IUser user = findByEmail(identity.getEmail());
            if (identity.isImpersonating()) {
                // cannot be simply reloaded from DB, impersonated user holds a subset of roles and authorities.
                // this reloads the impersonated user's roles as they are not complete (LoggedUser creates incomplete ProcessRole objects)
                IUser impersonated = identity.getImpersonated().transformToUser();
                // todo 2058
//                impersonated.setRoles(roleService.findByIds(loggedUser.getImpersonated().getRoles()));
                user.setImpersonated(impersonated);
            }
            return user;
        }
        return identity.transformToAnonymousUser();
    }

    @Override
    public Identity getAnonymousLogged() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(IdentityProperties.ANONYMOUS_AUTH_KEY)) {
            return getLoggedUser().transformToLoggedUser();
        }
        return getLoggedUserFromContext();
    }

    @Override
    public Identity getLoggedUserFromContext() {
        return (Identity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void deleteUser(IUser user) {
        User dbUser = (User) user;
        if (userRepository.findById(dbUser.getStringId()).isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not find user with id [%s]", dbUser.getId()));
        }
        roleAssignmentService.removeAssignmentsByActor(user.getStringId());
        userRepository.delete(dbUser);
    }
}