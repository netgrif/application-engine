package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class RegistrationService implements IRegistrationService {

    private static final Logger log = Logger.getLogger(RegistrationService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupService groupService;

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void removeExpiredUsers() {
        log.info("Removing expired unactivated invited users");
        List<User> expired = userRepository.removeAllByStateAndExpirationDateBefore(UserState.INVITED, LocalDateTime.now());
        log.info("Removed " + expired.size() + " unactivated users");
    }

    @Override
    public boolean verifyToken(String email, String token) {
        User user = userRepository.findByEmail(email);
        return user != null && Objects.equals(user.getToken(), token) && user.getExpirationDate().isAfter(LocalDateTime.now());
    }

    @Override
    public String getEmailToToken(String token) {
        User user = userRepository.findByToken(token);
        return user != null ? user.getEmail() : null;
    }

    @Override
    @Transactional
    public User createNewUser(NewUserRequest newUser) {
        User user = new User(newUser.email, null, User.UNKNOWN, User.UNKNOWN);
        user.setToken(new BigInteger(260, new SecureRandom()).toString(32));
        user.setExpirationDate(LocalDateTime.now().plusDays(3));
        user.setState(UserState.INVITED);
        userService.addDefaultRole(user);
        userService.addDefaultAuthorities(user);

        if (newUser.groups != null && !newUser.groups.isEmpty()) {
            user.setGroups(groupService.findAllById(newUser.groups));
        }
        if (newUser.processRoles != null && !newUser.processRoles.isEmpty()) {
            user.setUserProcessRoles(new HashSet<>(userProcessRoleRepository.findByRoleIdIn(newUser.processRoles)));
        }

        List<User> deleted = userRepository.removeByEmail(user.getEmail());
        if (deleted != null && !deleted.isEmpty())
            deleted.forEach(u -> log.info("Removed duplicate invitation for user with email " + u.getEmail()));
        return userRepository.save(user);
    }

    @Override
    public User registerUser(RegistrationRequest registrationRequest) {
        User user = userRepository.findByEmail(registrationRequest.email);
        if (user == null)
            return null;

        user.setName(registrationRequest.name);
        user.setSurname(registrationRequest.surname);
        user.setPassword(registrationRequest.password);

        user.setToken(null);
        user.setExpirationDate(null);
        user.setState(UserState.ACTIVE);

        return userService.saveNew(user);
    }
}
