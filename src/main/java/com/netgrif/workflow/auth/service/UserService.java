package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.Organization;
import com.netgrif.workflow.auth.domain.Role;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.repositories.RoleRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User saveNew(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if (user.getRoles().isEmpty()) {
            HashSet<Role> roles = new HashSet<Role>();
            roles.add(roleRepository.findByName("user"));
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    @Override
    public User save(User user){
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id){
        return userRepository.findOne(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByOrganizations(Set<Long> org){
        return userRepository.findByOrganizationsIn(org.stream().map(orgaz -> {
            Organization organization = new Organization();
            organization.setId(orgaz);
            return organization;
        }).collect(Collectors.toList()));
    }

    @Override
    public List<User> findByProcessRole(String roleId) {
        return userRepository.findByUserProcessRoles_RoleId(roleId);
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        User user = userRepository.findOne(userId);
        Role role = roleRepository.findOne(roleId);

        user.addRole(role);
        role.addUser(user);

        userRepository.save(user);
    }
}