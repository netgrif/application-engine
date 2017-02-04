package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Role;
import com.fmworkflow.auth.domain.RoleRepository;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if(user.getRoles().isEmpty()) {
            HashSet<Role> roles = new HashSet<Role>();
            roles.add(roleRepository.findByName("user"));
            user.setRoles(roles);
        }
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String email) {
        return userRepository.findByEmail(email);
    }
}
