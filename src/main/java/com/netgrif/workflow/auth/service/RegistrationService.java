package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.workflow.orgstructure.service.IGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class RegistrationService implements IRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProcessRoleRepository userProcessRoleRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private INextGroupService nextGroupService;

    @Value("${server.auth.token-validity-period}")
    private int tokenValidityPeriod;

    @Value("${server.auth.minimal-password-length}")
    private int minimalPasswordLength;

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void removeExpiredUsers() {
        log.info("Removing expired unactivated invited users");
        List<User> expired = userRepository.removeAllByStateAndExpirationDateBefore(UserState.INVITED, LocalDateTime.now());
        log.info("Removed " + expired.size() + " unactivated users");
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void resetExpiredToken() {
        log.info("Resetting expired user tokens");
        List<User> users = userRepository.findAllByStateAndExpirationDateBefore(UserState.BLOCKED, LocalDateTime.now());
        if (users == null || users.isEmpty()) {
            log.info("There are none expired tokens. Everything is awesome.");
            return;
        }

        users.forEach(user -> {
            user.setToken(null);
            user.setExpirationDate(null);
        });
        users = userRepository.saveAll(users);
        log.info("Reset " + users.size() + " expired user tokens");
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userService.encodeUserPassword(user);
        userRepository.save(user);
        log.info("Changed password for user " + user.getEmail() + ".");
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            log.info("Verifying token:" + token);
            String[] tokenParts = decodeToken(token);
            User user = userRepository.findByEmail(tokenParts[0]);
            return user != null && Objects.equals(user.getToken(), tokenParts[1]) && user.getExpirationDate().isAfter(LocalDateTime.now());
        } catch (InvalidUserTokenException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public User createNewUser(NewUserRequest newUser) {
        User user;
        if (userRepository.existsByEmail(newUser.email)) {
            user = userRepository.findByEmail(newUser.email);
            if (user.isRegistered())
                return null;
            log.info("Renewing old user [" + newUser.email + "]");
        } else {
            user = new User(newUser.email, null, User.UNKNOWN, User.UNKNOWN);
            log.info("Creating new user [" + newUser.email + "]");
        }
        user.setToken(generateTokenKey());
        user.setExpirationDate(generateExpirationDate());
        user.setState(UserState.INVITED);
        userService.addDefaultRole(user);
        userService.addDefaultAuthorities(user);

        if (newUser.groups != null && !newUser.groups.isEmpty()) {
            user.setGroups(groupService.findAllById(newUser.groups));
        }
        if (newUser.processRoles != null && !newUser.processRoles.isEmpty()) {
            user.setUserProcessRoles(new HashSet<>(userProcessRoleRepository.findByRoleIdIn(newUser.processRoles)));
        }

        User saved = userRepository.save(user);
        saved.setGroups(user.getGroups());
        userService.upsertGroupMember(saved);
        return saved;
    }

    @Override
    public User registerUser(RegistrationRequest registrationRequest) {
        log.info("Registering user " + registrationRequest.email);
        User user = userRepository.findByEmail(registrationRequest.email);
        if (user == null)
            return null;

        user.setName(registrationRequest.name);
        user.setSurname(registrationRequest.surname);
        user.setPassword(registrationRequest.password);

        user.setToken(null);
        user.setExpirationDate(null);
        user.setState(UserState.ACTIVE);

        nextGroupService.createGroup(user.getFullName(), user);
        return userService.saveNew(user);
    }

    @Override
    public User resetPassword(String email) {
        log.info("Resetting password of " + email);
        User user = userRepository.findByEmail(email);
        if (user == null || !user.isRegistered()) {
            String state = user == null ? "Non-existing" : "Inactive";
            log.info(state + " user [" + email + "] tried to reset his password");
            return null;
        }

        user.setState(UserState.BLOCKED);
        user.setPassword(null);
        user.setToken(generateTokenKey());
        user.setExpirationDate(generateExpirationDate());
        return userService.save(user);
    }

    @Override
    public User recover(String email, String newPassword) {
        log.info("Recovering user " + email);
        User user = userRepository.findByEmail(email);
        if (user == null)
            return null;

        user.setState(UserState.ACTIVE);
        user.setPassword(newPassword);
        userService.encodeUserPassword(user);
        user.setToken(null);
        user.setExpirationDate(null);

        return userService.save(user);
    }

    @Override
    public String generateTokenKey() {
        return new BigInteger(256, new SecureRandom()).toString(32);
    }

    @Override
    public String[] decodeToken(String token) throws InvalidUserTokenException {
        String[] parts = new String(Base64.getDecoder().decode(token)).split(":");
        if (parts.length != 2 || !parts[0].contains("@"))
            throw new InvalidUserTokenException(token);
        return parts;
    }

    @Override
    public String encodeToken(String email, String tokenKey) {
        return Base64.getEncoder().encodeToString((email + ":" + tokenKey).getBytes());
    }

    @Override
    public LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusDays(tokenValidityPeriod);
    }

    @Override
    public boolean isPasswordSufficient(String password) {
        return password.length() >= minimalPasswordLength;
    }
}
