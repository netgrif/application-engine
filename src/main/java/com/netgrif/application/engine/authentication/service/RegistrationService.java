package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService implements IRegistrationService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final INextGroupService groupService;
    private final IRoleService roleService;
    private final IActorService actorService;
    private final ServerAuthProperties serverAuthProperties;
    private final IIdentityService identityService;

    private static final String TOKEN_DELIMITER = ":";

    /**
     * todo javadoc
     * */
    @Override
    @Transactional
    public Identity createNewIdentity(NewIdentityRequest request) {
        IdentityParams identityParams = IdentityParams.with()
                .username(new TextField(request.email))
                .state(new EnumerationMapField(IdentityState.INVITED.name()))
                .expirationDateTime(new DateTimeField(generateExpirationDate()))
                .registrationToken(new TextField(generateTokenKey()))
                .build();
        Optional<Identity> identityOpt = identityService.findByUsername(request.email);
        Identity identity;

        if (identityOpt.isPresent()) {
            if (identityOpt.get().isActive()) {
                return null;
            }
            log.info("Renewing old identity [{}]", request.email);
            identity = identityService.update(identityOpt.get(), identityParams);
        } else {
            log.info("Creating new identity [{}]", request.email);
            identity = identityService.createWithDefaultActor(identityParams);

            if (request.roles != null && !request.roles.isEmpty()) {
                roleService.assignRolesToActor(identity.getMainActorId(), request.roles);
            }
            // todo 2058 add authorities (default app roles)
            roleService.assignRolesToActor(identity.getMainActorId(), Set.of(roleService.findDefaultRole().getStringId()));

            // todo 2058 groups
//            if (newUser.groups != null && !newUser.groups.isEmpty()) {
//                for (String group : newUser.groups) {
//                    groupService.addUser(user, group);
//                }
//            }
        }

        return identity;
    }

    /**
     * todo javadoc
     * */
    @Override
    @Transactional
    public Identity registerIdentity(RegistrationRequest registrationRequest) throws InvalidIdentityTokenException {
        String email = decodeToken(registrationRequest.token)[0];
        log.info("Registering user {}", email);

        Optional<Identity> identityOpt = identityService.findByUsername(email);
        if (identityOpt.isEmpty()) {
            return null;
        }

        Identity identity = identityOpt.get();
        IdentityParams identityParams = IdentityParams.with()
                .password(new TextField(registrationRequest.password))
                .firstname(new TextField(registrationRequest.name))
                .lastname(new TextField(registrationRequest.surname))
                .registrationToken(new TextField(null))
                .expirationDateTime(new DateTimeField(null))
                .state(new EnumerationMapField(IdentityState.ACTIVE.name()))
                .build();
        identity = identityService.encodePasswordAndUpdate(identity, identityParams);

        Optional<Actor> actorOpt = actorService.findById(identity.getMainActorId());
        if (actorOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Identity [%s] has no default actor!", identity.getStringId()));
        }

        ActorParams actorParams = ActorParams.with()
                .firstname(new TextField(registrationRequest.name))
                .lastname(new TextField(registrationRequest.surname))
                .build();
        actorService.update(actorOpt.get(), actorParams);

        return identity;
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean verifyToken(String token) {
        try {
            log.info("Verifying token: {}", token);
            String[] tokenParts = decodeToken(token);
            String email = tokenParts[0];
            String decodedToken = tokenParts[1];
            Optional<Identity> identityOpt = identityService.findByUsername(email);
            return identityOpt.isPresent()
                    && Objects.equals(identityOpt.get().getRegistrationToken(), decodedToken)
                    && identityOpt.get().getExpirationDate().isAfter(LocalDateTime.now());
        } catch (InvalidIdentityTokenException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * todo javadoc
     * */
    @Override
    public String generateTokenKey() {
        return new BigInteger(256, new SecureRandom()).toString(32);
    }

    /**
     * todo javadoc
     * */
    @Override
    public String[] decodeToken(String token) throws InvalidIdentityTokenException {
        if (token == null || token.isEmpty()) {
            throw new InvalidIdentityTokenException(token);
        }
        byte[] decodedBytes;

        try {
            decodedBytes = Base64.getDecoder().decode(token);
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdentityTokenException(token);
        }
        String decodedString = new String(decodedBytes);
        String[] parts = decodedString.split(TOKEN_DELIMITER);

        if (parts.length != 2 || !parts[0].contains("@")) {
            throw new InvalidIdentityTokenException(token);
        }

        return parts;
    }

    /**
     * todo javadoc
     * */
    @Override
    public String encodeToken(String email, String tokenKey) {
        return Base64.getEncoder().encodeToString((email + TOKEN_DELIMITER + tokenKey).getBytes());
    }

    /**
     * todo javadoc
     * */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void removeExpiredIdentities() {
        log.info("Removing expired unactivated identities");
        List<Identity> expired = identityService.removeAllByStateAndExpirationDateBefore(IdentityState.INVITED,
                LocalDateTime.now());
        log.info("Removed {} unactivated identities", expired.size());
    }

    /**
     * todo javadoc
     * */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void resetExpiredToken() {
        log.info("Resetting expired identity tokens");
        List<Identity> identities = identityService.findAllByStateAndExpirationDateBefore(IdentityState.BLOCKED,
                LocalDateTime.now());
        if (identities.isEmpty()) {
            log.info("There are none expired tokens. Everything is awesome.");
            return;
        }

        identities.forEach(identity -> {
            identityService.update(identity, IdentityParams.with()
                    .registrationToken(new TextField(null))
                    .expirationDateTime(new DateTimeField(null))
                    .build());
        });

        log.info("Reset {} expired identity tokens", identities.size());
    }

    /**
     * todo javadoc
     * */
    @Override
    public void changePassword(Identity identity, String newPassword) {
        identityService.encodePasswordAndUpdate(identity, IdentityParams.with()
                .password(new TextField(newPassword))
                .build());
        log.info("Changed password for identity {}.", identity.getUsername());
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean matchesIdentityPassword(Identity identity, String passwordToCompare) {
        return bCryptPasswordEncoder.matches(passwordToCompare, identity.getPassword());
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity resetPassword(String email) {
        log.info("Resetting password of {}", email);

        Optional<Identity> identityOpt = identityService.findByUsername(email);
        if (identityOpt.isEmpty() || !identityOpt.get().isActive()) {
            String state = identityOpt.isEmpty() ? "Non-existing" : "Inactive";
            log.info("{} identity [{}] tried to reset his password", state, email);
            return null;
        }
        IdentityParams params = IdentityParams.with()
                .state(new EnumerationMapField(IdentityState.BLOCKED.name()))
                .password(new TextField(null))
                .registrationToken(new TextField(generateTokenKey()))
                .expirationDateTime(new DateTimeField(generateExpirationDate()))
                .build();

        return identityService.update(identityOpt.get(), params);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity recover(String email, String newPassword) {
        log.info("Recovering identity {}", email);

        Optional<Identity> identityOpt = identityService.findByUsername(email);
        if (identityOpt.isEmpty()) {
            return null;
        }
        IdentityParams params = IdentityParams.with()
                .state(new EnumerationMapField(IdentityState.ACTIVE.name()))
                .password(new TextField(newPassword))
                .registrationToken(new TextField(null))
                .expirationDateTime(new DateTimeField(null))
                .build();

        return identityService.encodePasswordAndUpdate(identityOpt.get(), params);
    }

    /**
     * todo javadoc
     * */
    @Override
    public LocalDateTime generateExpirationDate() {
        return LocalDateTime.now().plusDays(serverAuthProperties.getTokenValidityPeriod());
    }

    /**
     * todo javadoc
     * */
    @Override
    public boolean isPasswordSufficient(String password) {
        // todo: release/8.0.0 is this enough?
        return password.length() >= serverAuthProperties.getMinimalPasswordLength();
    }
}
