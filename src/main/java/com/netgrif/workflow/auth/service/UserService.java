package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.workflow.orgstructure.service.IMemberService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.startup.SystemUserRunner;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    protected IProcessRoleService processRoleService;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @Autowired
    protected IMemberService memberService;

    @Autowired
    protected INextGroupService groupService;

    @Autowired
    protected IRegistrationService registrationService;

    @Override
    public IUser saveNew(IUser user) {
        registrationService.encodeUserPassword((RegisteredUser) user);
        addDefaultRole(user);
        addDefaultAuthorities(user);

        User savedUser = userRepository.save((User) user);
        groupService.createGroup((User) user);
        groupService.addUserToDefaultGroup((User) user);
        savedUser.setGroups(user.getGroups());
        upsertGroupMember(savedUser);
        publisher.publishEvent(new UserRegistrationEvent(savedUser));
        return savedUser;
    }

    @Override
    public AnonymousUser saveNewAnonymous(AnonymousUser user) {
        addDefaultRole(user);
        addDefaultAuthorities(user);

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

    @Override
    public Optional<IUser> get(String id) {
        return Optional.ofNullable(userRepository.findById(id).orElse(null));
    }

    @Override
    public IUser findByAuth(Authentication auth) {
        return findByEmail(auth.getName(), false);
    }

    @Override
    public IUser save(IUser user) {
        return userRepository.save((User) user);
    }

    @Override
    public IUser findById(String id, boolean small) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent())
            throw new IllegalArgumentException("Could not find user with id ["+id+"]");
        /*if (!small) {
            loadGroups(user.get());
            return loadProcessRoles(user.get());
        }*/
        return user.get();
    }

    @Override
    public IUser resolveById(String id, boolean small) {
        return findById(id, small);
    }

    @Override
    public IUser findByEmail(String email, boolean small) {
        User user = userRepository.findByEmail(email);
        /*if (!small) {
            loadGroups(user);
            return loadProcessRoles(user);
        }*/
        return user;
    }

    @Override
    public IUser findAnonymousByEmail(String email, boolean small) {
        return findByEmail(email, small);
    }

    @Override
    public List<IUser> findAll(boolean small) {
        List<User> users = userRepository.findAll();
//        if (!small) users.forEach(this::loadProcessRoles);
        return changeType(users);
    }

    @Override
    public Page<IUser> findAllCoMembers(LoggedUser loggedUser, boolean small, Pageable pageable) {
        // TODO: 8/27/18 make all pageable
        Set<String> members = memberService.findAllCoMembersIds(loggedUser.getEmail());
        members.add(loggedUser.getId());
        Set<ObjectId> objMembers = members.stream().map(ObjectId::new).collect(Collectors.toSet());
        Page<User> users = userRepository.findAllBy_idInAndState(objMembers, UserState.ACTIVE, pageable);
        /*if (!small)
            users.forEach(this::loadProcessRoles);*/
        return changeType(users, pageable);
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, LoggedUser loggedUser, Boolean small, Pageable pageable) {
        Set<String> members = memberService.findAllCoMembersIds(loggedUser.getEmail());
        members.add(loggedUser.getId());

        Page<User> users = userRepository.findAll(buildPredicate(members.stream().map(ObjectId::new)
                .collect(Collectors.toSet()), query), pageable);
        /*if (!small)
            users.forEach(this::loadProcessRoles);*/
        return changeType(users, pageable);
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, List<ObjectId> roleIds, List<ObjectId> negateRoleIds, LoggedUser loggedUser, Boolean small, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty()))
            return searchAllCoMembers(query, loggedUser, small, pageable);

        if (negateRoleIds == null) {
            negateRoleIds = new ArrayList<>();
        }

        Set<String> members = memberService.findAllCoMembersIds(loggedUser.getEmail());
        members.add(loggedUser.getId());
        BooleanExpression predicate = buildPredicate(members.stream().map(ObjectId::new).collect(Collectors.toSet()), query);
        if (!(roleIds == null || roleIds.isEmpty())) {
            predicate = predicate.and(QUser.user.processRoles.any()._id.in(roleIds));
        }
        predicate = predicate.and(QUser.user.processRoles.any()._id.in(negateRoleIds).not());
        Page<User> users = userRepository.findAll(predicate, pageable);
        /*if (!small)
            users.forEach(this::loadProcessRoles);*/
        return changeType(users, pageable);
    }

    private BooleanExpression buildPredicate(Set<ObjectId> members, String query) {
        BooleanExpression predicate = QUser.user
                ._id.in(members)
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
    public Page<IUser> findAllActiveByProcessRoles(Set<String> roleIds, boolean small, Pageable pageable) {
        Page<User> users = userRepository.findDistinctByStateAndProcessRoles__idIn(UserState.ACTIVE, new ArrayList<>(roleIds), pageable);
        /*if (!small) {
            users.forEach(this::loadProcessRoles);
        }*/
        return changeType(users, pageable);
    }

    @Override
    public List<IUser> findAllByProcessRoles(Set<String> roleIds, boolean small) {
        List<User> users = userRepository.findAllByProcessRoles__idIn(new ArrayList<>(roleIds));
        /*if (!small) {
            users.forEach(this::loadProcessRoles);
        }*/
        return changeType(users);
    }

    @Override
    public IUser getLoggedOrSystem() {
        try {
            return getLoggedUser();
        } catch (NullPointerException e) {
            return userRepository.findByEmail(SystemUserRunner.SYSTEM_USER_EMAIL);
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
            return findByEmail(loggedUser.getEmail(), false);
        }
        return loggedUser.transformToAnonymousUser();
    }

    @Override
    public void deleteUser(IUser user) {
        User dbUser = (User) user;
        if (!userRepository.findById(dbUser.getStringId()).isPresent())
            throw new IllegalArgumentException("Could not find user with id [" + dbUser.get_id() + "]");
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