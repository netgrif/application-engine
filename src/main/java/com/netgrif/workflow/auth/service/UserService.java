package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;
import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.orgstructure.service.IMemberService;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        publisher.publishEvent(new UserRegistrationEvent(savedUser));
        return savedUser;
    }

    private void encodeUserPassword(User user) {
        String pass = user.getPassword();
        if (pass == null)
            throw new IllegalArgumentException("User has no password");
        user.setPassword(bCryptPasswordEncoder.encode(pass));
    }

    private void addDefaultRole(User user) {
        user.addProcessRole(userProcessRoleService.findDefault());
    }

    private void addDefaultAuthorities(User user) {
        if (user.getAuthorities().isEmpty()) {
            HashSet<Authority> authorities = new HashSet<Authority>();
            authorities.add(authorityRepository.findByName(Authority.user));
            user.setAuthorities(authorities);
        }
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id, boolean small) {
        User user = userRepository.findOne(id);
        if (!small) return loadProcessRoles(user);
        return user;
    }

    @Override
    public List<User> findAll(boolean small) {
        List<User> users = userRepository.findAll();
        if (!small) users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Set<User> findByGroups(Set<Long> groups, boolean small) {
        Set<Member> members = memberService.findByGroups(groups);
        Set<User> users = new HashSet<>(userRepository.findAll(members.parallelStream().map(Member::getUserId).collect(Collectors.toList())));
        if (!small) users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Set<User> findByProcessRoles(Set<String> roleIds, boolean small) {
        Set<User> users = new HashSet<>(userRepository.findByUserProcessRoles_RoleIdIn(new ArrayList<>(roleIds)));
        if (!small)
            users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public void assignAuthority(Long userId, Long authorityId) {
        User user = userRepository.findOne(userId);
        Authority authority = authorityRepository.findOne(authorityId);

        user.addAuthority(authority);
        authority.addUser(user);

        userRepository.save(user);
    }

    private User loadProcessRoles(User user) {
        user.setProcessRoles(processRoleRepository.findAll(user.getUserProcessRoles()
                .stream().map(UserProcessRole::getRoleId).collect(Collectors.toList())));
        return user;
    }
}