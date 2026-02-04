package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;

@Slf4j
public class RegistrationService implements IRegistrationService {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ProcessRoleService processRole;

    @Autowired
    private SecurityConfigurationProperties.AuthProperties serverAuthProperties;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private PaginationProperties paginationProperties;

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

        Pageable pageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Page<User> users;
        int totalReset = 0;
        do {
            users = userService.findAllByStateAndExpirationDateBefore(UserState.BLOCKED, LocalDateTime.now(), null, pageable);
            if (users == null || users.isEmpty()) {
                log.info("There are none expired tokens. Everything is awesome.");
                return;
            }

            users.forEach(user -> {
                user.setToken(null);
                user.setExpirationDate(null);
            });
            userService.saveUsers(users.getContent().stream().map(AbstractUser.class::cast).toList());
            totalReset += users.getContent().size();

            pageable = pageable.next();
        } while (users.hasNext());

        log.info("Reset {} expired user tokens", totalReset);
    }

    @Override
    public void changePassword(AbstractUser user, String newPassword) {
        user.setPassword(newPassword);
        encodeUserPassword(user);
        userService.saveUser(user, null);
        log.info("Changed password for user {}.", user.getEmail());
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            log.debug("Verifying token: {}", token);
            String[] tokenParts = decodeToken(token);
            User user = (User) userService.findByEmail(tokenParts[0], null);
            return user != null && Objects.equals(user.getToken(), tokenParts[1]) && user.getExpirationDate().isAfter(LocalDateTime.now());
        } catch (InvalidUserTokenException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public void encodeUserPassword(AbstractUser user) {
        String pass = user.getPassword();
        if (pass == null) {
            throw new IllegalArgumentException("User has no password");
        }
        user.setPassword(passwordEncoder.encode(pass));
    }

    @Override
    public boolean stringMatchesUserPassword(AbstractUser user, String passwordToCompare) {
        return passwordEncoder.matches(passwordToCompare, user.getPassword());
    }

    @Override
    @Transactional
    public AbstractUser createNewUser(NewUserRequest newUser) {
        User user = (User) userService.findByEmail(newUser.email, null);
        if (user != null) {
            if (user.isActive()) {
                return null;
            }
            log.info("Renewing old user [{}]", newUser.email);
        } else {
            user = new User();
            user.setEmail(newUser.email);
            user.setUsername(newUser.email);
            log.info("Creating new user [{}]", newUser.email);
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
                groupService.addUser(user, group);
            }
        }

        return user;
    }

    @Override
    public AbstractUser registerUser(RegistrationRequest registrationRequest) throws InvalidUserTokenException {
        String email = decodeToken(registrationRequest.token)[0];
        log.info("Registering user {}", email);
        User user = (User) userService.findByEmail(email, null);
        if (user == null) {
            return null;
        }

        user.setFirstName(registrationRequest.name);
        user.setLastName(registrationRequest.surname);
        user.setPassword(registrationRequest.password);

        user.setToken(StringUtils.EMPTY);
        user.setExpirationDate(null);
        user.setState(UserState.ACTIVE);

        return userService.saveUser(user, null);
    }

    @Override
    public AbstractUser resetPassword(String email) {
        log.info("Resetting password of {}", email);
        User user = (User) userService.findByEmail(email, null);
        if (user == null || !user.isActive()) {
            String state = user == null ? "Non-existing" : "Inactive";
            log.info("{} user [{}] tried to reset his password", state, email);
            return null;
        }

        user.setState(UserState.BLOCKED);
        user.setPassword(null);
        user.setToken(generateTokenKey());
        user.setExpirationDate(generateExpirationDate());
        return userService.saveUser(user, null);
    }

    @Override
    public AbstractUser recover(String email, String newPassword) {
        log.info("Recovering user {}", email);
        User user = (User) userService.findByEmail(email, null);
        if (user == null) {
            return null;
        }
        user.setState(UserState.ACTIVE);
        user.setPassword(newPassword);
        encodeUserPassword(user);
        user.setToken(null);
        user.setExpirationDate(null);

        return userService.saveUser(user, null);
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
