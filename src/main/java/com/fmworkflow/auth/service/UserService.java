package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Role;
import com.fmworkflow.auth.domain.repositories.RoleRepository;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.repositories.UserRepository;
import com.fmworkflow.auth.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
        return userRepository.getOne(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByOrganizations(List<Long> org){
        return userRepository.findByOrganizationsIn(org);
    }

    @Override
    public List<User> findByProcessRole(String roleId) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getUserProcessRoles()
                        .stream()
                        .anyMatch(role -> role.getRoleId().equals(roleId)))
                .collect(Collectors.toList());
    }

    @Override
    public void assignRole(String userEmail, Long roleId) {
        User user = userRepository.findByEmail(userEmail);
        Role role = roleRepository.findOne(roleId);

        user.addRole(role);
        role.addUser(user);

        userRepository.save(user);
    }
}