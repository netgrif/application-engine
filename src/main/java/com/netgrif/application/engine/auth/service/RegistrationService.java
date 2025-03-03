package com.netgrif.application.engine.auth.service;

import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.RegisteredUser;
import com.netgrif.core.auth.domain.User;
import com.netgrif.core.auth.domain.enums.UserState;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.auth.service.UserService;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.auth.service.GroupService;
import com.netgrif.adapter.petrinet.service.ProcessRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistrationService implements IRegistrationService {

    @Autowired
    protected BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ProcessRoleService processRole;

    @Autowired
    private ServerAuthProperties serverAuthProperties;
    @Autowired
    private ProcessRoleService processRoleService;

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void removeExpiredUsers() {
        log.info("Removing expired unactivated invited users");
        userService.removeAllByStateAndExpirationDateBefore(UserState.INACTIVE, LocalDateTime.now(),null);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void resetExpiredToken() {
        log.info("Resetting expired user tokens");
        List<User> users = userService.findAllByStateAndExpirationDateBefore(UserState.BLOCKED, LocalDateTime.now(), null);
        if (users == null || users.isEmpty()) {
            log.info("There are none expired tokens. Everything is awesome.");
            return;
        }

        users.forEach(user -> {
            user.setToken(null);
            user.setExpirationDate(null);
        });
        users = userService.saveUsers(users.stream().map(u -> (IUser) u).collect(Collectors.toList()));
        log.info("Reset " + users.size() + " expired user tokens");
    }

    @Override
    public void changePassword(RegisteredUser user, String newPassword) {
        user.setPassword(newPassword);
        encodeUserPassword(user);
        userService.saveUser(user, null);
        log.info("Changed password for user " + user.getEmail() + ".");
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            log.info("Verifying token:" + token);
            String[] tokenParts = decodeToken(token);
            User user = (User) userService.findByEmail(tokenParts[0], null);
            return user != null && Objects.equals(user.getToken(), tokenParts[1]) && user.getExpirationDate().isAfter(LocalDateTime.now());
        } catch (InvalidUserTokenException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public void encodeUserPassword(RegisteredUser user) {
        String pass = user.getPassword();
        if (pass == null) {
            throw new IllegalArgumentException("User has no password");
        }
        user.setPassword(bCryptPasswordEncoder.encode(pass));
    }

    @Override
    public boolean stringMatchesUserPassword(RegisteredUser user, String passwordToCompare) {
        return bCryptPasswordEncoder.matches(passwordToCompare, user.getPassword());
    }

    @Override
    @Transactional
    public User createNewUser(NewUserRequest newUser) {
        User user = (User) userService.findByEmail(newUser.email, null);
        if (user != null) {
            if (user.isActive()) {
                return null;
            }
            log.info("Renewing old user [" + newUser.email + "]");
        } else {
            user = new com.netgrif.adapter.auth.domain.User();
            user.setEmail(newUser.email);
            log.info("Creating new user [" + newUser.email + "]");
        }
        user.setToken(generateTokenKey());
        user.setPassword("");
        user.setExpirationDate(generateExpirationDate());
        user.setState(UserState.INACTIVE);
        userService.addDefaultAuthorities(user);

        if (newUser.processRoles != null && !newUser.processRoles.isEmpty()) {
            user.setProcessRoles(new HashSet<>(processRole.findByIds(newUser.processRoles)));
        }
        userService.addRole(user, processRoleService.getDefaultRole().getStringId());
        user = (User) userService.saveUser(user, null);

        if (newUser.groups != null && !newUser.groups.isEmpty()) {
            for (String group : newUser.groups) {
                groupService.addUser((IUser) user, group);
            }
        }

        return user;
    }

    @Override
    public RegisteredUser registerUser(RegistrationRequest registrationRequest) throws InvalidUserTokenException {
        String email = decodeToken(registrationRequest.token)[0];
        log.info("Registering user " + email);
        RegisteredUser user = (RegisteredUser) userService.findByEmail(email, null);
        if (user == null) {
            return null;
        }

        user.setFirstName(registrationRequest.name);
        user.setLastName(registrationRequest.surname);
        user.setPassword(registrationRequest.password);

        user.setToken(null);
        user.setExpirationDate(null);
        user.setState(UserState.ACTIVE);

        return (RegisteredUser) userService.saveNewAndAuthenticate(user, null);
    }

    @Override
    public RegisteredUser resetPassword(String email) {
        log.info("Resetting password of " + email);
        User user = (User) userService.findByEmail(email, null);
        if (user == null || !user.isActive()) {
            String state = user == null ? "Non-existing" : "Inactive";
            log.info(state + " user [" + email + "] tried to reset his password");
            return null;
        }

        user.setState(UserState.BLOCKED);
        user.setPassword(null);
        user.setToken(generateTokenKey());
        user.setExpirationDate(generateExpirationDate());
        return (RegisteredUser) userService.saveUser(user, null);
    }

    @Override
    public RegisteredUser recover(String email, String newPassword) {
        log.info("Recovering user " + email);
        User user = (User) userService.findByEmail(email, null);
        if (user == null) {
            return null;
        }
        user.setState(UserState.ACTIVE);
        user.setPassword(newPassword);
        encodeUserPassword(user);
        user.setToken(null);
        user.setExpirationDate(null);

        return (RegisteredUser) userService.saveUser(user, null);
    }

    @Override
    public String generateTokenKey() {
        return new BigInteger(256, new SecureRandom()).toString(32);
    }

    @Override
    public String[] decodeToken(String token) throws InvalidUserTokenException {
        if (token == null || token.isEmpty()) {
            throw new InvalidUserTokenException(token);
        }
        byte[] decodedBytes;

        try {
            decodedBytes = Base64.getDecoder().decode(token);
        } catch (IllegalArgumentException exception) {
            throw new InvalidUserTokenException(token);
        }
        String decodedString = new String(decodedBytes);
        String[] parts = decodedString.split(":");

        if (parts.length != 2 || !parts[0].contains("@")) {
            throw new InvalidUserTokenException(token);
        }

        return parts;
    }

    @Override
    public String encodeToken(String email, String tokenKey) {
        return Base64.getEncoder().encodeToString((email + ":" + tokenKey).getBytes());
    }

    @Override
    public LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusDays(serverAuthProperties.getTokenValidityPeriod());
    }

    @Override
    public boolean isPasswordSufficient(String password) {
        return password.length() >= serverAuthProperties.getMinimalPasswordLength();
    }
}
