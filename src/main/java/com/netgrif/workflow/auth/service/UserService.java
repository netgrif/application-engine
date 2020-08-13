package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;
import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.orgstructure.service.IMemberService;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.workflow.startup.SystemUserRunner;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private IUserProcessRoleService userProcessRoleService;

    @Autowired
    private IMemberService memberService;

    @Override
    public User saveNew(User user) {
        encodeUserPassword(user);
        addDefaultRole(user);
        addDefaultAuthorities(user);

        User savedUser = userRepository.save(user);
        savedUser.setGroups(user.getGroups());
        upsertGroupMember(savedUser);
        publisher.publishEvent(new UserRegistrationEvent(savedUser));
        return savedUser;
    }

    @Override
    public User update(User user, UpdateUserRequest updates) {
        if (updates.telNumber != null) {
            user.setTelNumber(updates.telNumber);
        }
        if (updates.avatar != null) {
            user.setAvatar(updates.avatar);
        }
        if (updates.name != null) {
            user.setName(updates.name);
        }
        if (updates.surname != null) {
            user.setSurname(updates.surname);
        }
        user = userRepository.save(user);
        return user;
    }

    @Override
    public Member upsertGroupMember(User user) {
        Member member = memberService.findByEmail(user.getEmail());
        if (member == null)
            member = new Member(user.getId(), user.getName(), user.getSurname(), user.getEmail());
        member.setGroups(user.getGroups());
        return memberService.save(member);
    }

    @Override
    public void encodeUserPassword(User user) {
        String pass = user.getPassword();
        if (pass == null)
            throw new IllegalArgumentException("User has no password");
        user.setPassword(bCryptPasswordEncoder.encode(pass));
    }

    @Override
    public boolean stringMatchesUserPassword(User user, String passwordToCompare) {
        return bCryptPasswordEncoder.matches(passwordToCompare, user.getPassword());
    }

    @Override
    public void addDefaultRole(User user) {
        user.addProcessRole(userProcessRoleService.findDefault());
    }

    @Override
    public void addDefaultAuthorities(User user) {
        if (user.getAuthorities().isEmpty()) {
            HashSet<Authority> authorities = new HashSet<Authority>();
            authorities.add(authorityRepository.findByName(Authority.user));
            user.setAuthorities(authorities);
        }
    }

    @Override
    public User findByAuth(Authentication auth) {
        return findByEmail(auth.getName(), false);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id, boolean small) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent())
            throw new IllegalArgumentException("Could not find user with id ["+id+"]");
        if (!small) {
            return loadProcessRoles(user.get());
        }
        return user.get();
    }

    @Override
    public User findByEmail(String email, boolean small) {
        User user = userRepository.findByEmail(email);
        if (!small)
            return loadProcessRoles(user);
        return user;
    }

    @Override
    public List<User> findAll(boolean small) {
        List<User> users = userRepository.findAll();
        if (!small) users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Page<User> findAllCoMembers(LoggedUser loggedUser, boolean small, Pageable pageable) {
        // TODO: 8/27/18 make all pageable
        Set<Long> members = memberService.findAllCoMembersIds(loggedUser.getEmail());
        members.add(loggedUser.getId());
        Page<User> users = userRepository.findAllByIdInAndState(members, UserState.ACTIVE, pageable);
        if (!small)
            users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Page<User> searchAllCoMembers(String query, LoggedUser loggedUser, Boolean small, Pageable pageable) {
        Set<Long> members = memberService.findAllCoMembersIds(loggedUser.getEmail());
        members.add(loggedUser.getId());
        BooleanExpression predicate = QUser.user
                .id.in(members)
                .and(QUser.user.state.eq(UserState.ACTIVE));
        for (String word : query.split(" ")) {
            predicate = predicate
                    .andAnyOf(QUser.user.email.containsIgnoreCase(word),
                              QUser.user.name.containsIgnoreCase(word),
                              QUser.user.surname.containsIgnoreCase(word));
        }

        Page<User> users = userRepository.findAll(predicate, pageable);
        if (!small)
            users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Page<User> searchAllCoMembers(String query, List<String> roleIds, LoggedUser loggedUser, Boolean small, Pageable pageable) {
        if (roleIds == null || roleIds.isEmpty())
            return searchAllCoMembers(query, loggedUser, small, pageable);

        Set<Long> members = memberService.findAllCoMembersIds(loggedUser.getEmail());
        members.add(loggedUser.getId());
        BooleanExpression predicate = QUser.user
                .id.in(members)
                .and(QUser.user.state.eq(UserState.ACTIVE))
                .and(QUser.user.userProcessRoles.any().roleId.in(roleIds))
                .andAnyOf(
                        QUser.user.email.containsIgnoreCase(query),
                        QUser.user.name.containsIgnoreCase(query),
                        QUser.user.surname.containsIgnoreCase(query));
        Page<User> users = userRepository.findAll(predicate, pageable);
        if (!small)
            users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Page<User> findAllActiveByProcessRoles(Set<String> roleIds, boolean small, Pageable pageable) {
        Page<User> users = userRepository.findDistinctByStateAndUserProcessRoles_RoleIdIn(UserState.ACTIVE, new ArrayList<>(roleIds), pageable);
        if (!small) {
            users.forEach(this::loadProcessRoles);
        }
        return users;
    }

    @Override
    public void assignAuthority(Long userId, Long authorityId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Authority> authority = authorityRepository.findById(authorityId);

        if (!user.isPresent())
            throw new IllegalArgumentException("Could not find user with id ["+userId+"]");
        if (!authority.isPresent())
            throw new IllegalArgumentException("Could not find authority with id ["+authorityId+"]");

        user.get().addAuthority(authority.get());
        authority.get().addUser(user.get());

        userRepository.save(user.get());
    }

    @Override
    public User getLoggedOrSystem() {
        try {
            return getLoggedUser();
        } catch (NullPointerException e) {
            return userRepository.findByEmail(SystemUserRunner.SYSTEM_USER_EMAIL);
        }
    }

    @Override
    public User getSystem() {
        return userRepository.findByEmail(SystemUserRunner.SYSTEM_USER_EMAIL);
    }

    @Override
    public User getLoggedUser() {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findByEmail(loggedUser.getEmail(), false);
    }

    @Override
    public User addRole(User user, String roleStringId) {
        UserProcessRole role = userProcessRoleService.findByRoleId(roleStringId);
        user.addProcessRole(role);
        return userRepository.save(user);
    }

    private User loadProcessRoles(User user) {
        if (user == null)
            return null;
        user.setProcessRoles(processRoleRepository.findAllById(user.getUserProcessRoles()
                .stream().map(UserProcessRole::getRoleId).collect(Collectors.toList())));
        return user;
    }
}