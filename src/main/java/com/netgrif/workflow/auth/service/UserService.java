package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.event.events.UserRegistrationEvent;
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
    private OrganizationRepository organizationRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public User saveNew(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if (user.getAuthorities().isEmpty()) {
            HashSet<Authority> authorities = new HashSet<Authority>();
            authorities.add(authorityRepository.findByName(Authority.user));
            user.setAuthorities(authorities);
        }

        User savedUser = userRepository.save(user);
        publisher.publishEvent(new UserRegistrationEvent(savedUser));
        return savedUser;
    }

    @Override
    public User save(User user){
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id, boolean small){
        User user = userRepository.findOne(id);
        if(!small) return loadProcessRoles(user);
        return user;
    }

    @Override
    public List<User> findAll(boolean small) {
        List<User> users = userRepository.findAll();
        if(!small) users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Set<User> findByOrganizations(Set<Long> org, boolean small){
        Set<User> users = new HashSet<>(userRepository.findByOrganizationsIn(org.stream()
                .map(Organization::new).collect(Collectors.toList())));
        if(!small) users.forEach(this::loadProcessRoles);
        return users;
    }

    @Override
    public Set<User> findByProcessRoles(Set<String> roleIds) {
        return new HashSet<>(userRepository.findByUserProcessRoles_RoleIdIn(new ArrayList<>(roleIds)));
    }

    @Override
    public void assignAuthority(Long userId, Long authorityId) {
        User user = userRepository.findOne(userId);
        Authority authority = authorityRepository.findOne(authorityId);

        user.addAuthority(authority);
        authority.addUser(user);

        userRepository.save(user);
    }

    @Override
    public List<Organization> getAllOrganizations(){
        return organizationRepository.findAll();
    }

    private User loadProcessRoles(User user){
        user.setProcessRoles(processRoleRepository.findAll(user.getUserProcessRoles()
                .stream().map(UserProcessRole::getRoleId).collect(Collectors.toList())));
        return user;
    }
}