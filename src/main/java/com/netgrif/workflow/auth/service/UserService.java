package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.Organization;
import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
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
    private AuthorityRepository authorityRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User saveNew(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if (user.getAuthorities().isEmpty()) {
            HashSet<Authority> authorities = new HashSet<Authority>();
            authorities.add(authorityRepository.findByName(Authority.user));
            user.setAuthorities(authorities);
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
            Organization organization = new Organization(orgaz);
            return organization;
        }).collect(Collectors.toList()));
    }

    @Override
    public List<User> findByProcessRole(String roleId) {
        return userRepository.findByUserProcessRoles_RoleId(roleId);
    }

    @Override
    public void assignAuthority(Long userId, Long authorityId) {
        User user = userRepository.findOne(userId);
        Authority authority = authorityRepository.findOne(authorityId);

        user.addAuthority(authority);
        authority.addUser(user);

        userRepository.save(user);
    }
}