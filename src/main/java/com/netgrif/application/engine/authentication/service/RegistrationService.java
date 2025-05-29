package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authentication.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.properties.ServerAuthProperties;
import com.netgrif.application.engine.event.events.authentication.IdentityRegistrationEvent;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.transaction.NaeTransaction;
import groovy.lang.Closure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RegistrationService implements IRegistrationService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final IRoleService roleService;
    private final IUserService userService;
    private final MongoTransactionManager transactionManager;
    private final ServerAuthProperties serverAuthProperties;
    private final IIdentityService identityService;
    private final ApplicationEventPublisher publisher;

    private static final String TOKEN_DELIMITER = ":";

    public RegistrationService(BCryptPasswordEncoder bCryptPasswordEncoder, IRoleService roleService,
                               @Lazy IUserService userService, MongoTransactionManager transactionManager,
                               ServerAuthProperties serverAuthProperties, IIdentityService identityService,
                               ApplicationEventPublisher publisher) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleService = roleService;
        this.userService = userService;
        this.transactionManager = transactionManager;
        this.serverAuthProperties = serverAuthProperties;
        this.identityService = identityService;
        this.publisher = publisher;
    }

    /**
     * todo javadoc
     * */
    @Override
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
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                NaeTransaction transaction = NaeTransaction.builder()
                        .transactionManager(transactionManager)
                        .event(new Closure<>(null) {
                            @Override
                            public Identity call() {
                                return doCreateNewIdentity(identityParams, request);
                            }
                        })
                        .build();
                transaction.begin();
                identity = (Identity) transaction.getResultOfEvent();
            } else {
                identity = doCreateNewIdentity(identityParams, request);
            }
        }

        return identity;
    }

    private Identity doCreateNewIdentity(IdentityParams identityParams, NewIdentityRequest request) {
        Identity identity = identityService.createWithDefaultUser(identityParams);

        if (request.roles != null && !request.roles.isEmpty()) {
            roleService.assignRolesToActor(identity.getMainActorId(), request.roles);
        }

        if (request.groups != null && !request.groups.isEmpty()) {
            Optional<User> userOpt = userService.findById(identity.getMainActorId());
            if (userOpt.isEmpty()) {
                throw new IllegalStateException(String.format("Cannot find user with id [%s], that was just created.",
                        identity.getMainActorId()));
            }
            userService.addGroups(userOpt.get(), request.groups);
        }

        publisher.publishEvent(new IdentityRegistrationEvent(identity));

        return identity;
    }

    /**
     * todo javadoc
     * */
    @Override
    public Identity registerIdentity(RegistrationRequest registrationRequest) throws InvalidIdentityTokenException {
        String email = decodeToken(registrationRequest.token)[0];
        log.info("Registering identity [{}]", email);

        Optional<Identity> identityOpt = identityService.findByUsername(email);
        if (identityOpt.isEmpty()) {
            return null;
        }

        Identity identity = identityOpt.get();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            Identity finalIdentity = identity;
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<>(null) {
                        @Override
                        public Identity call() {
                            return doRegisterIdentity(finalIdentity, registrationRequest);
                        }
                    })
                    .build();
            transaction.begin();
            identity = (Identity) transaction.getResultOfEvent();
        } else {
            identity = doRegisterIdentity(identity, registrationRequest);
        }

        return identity;
    }

    private Identity doRegisterIdentity(Identity identity, RegistrationRequest registrationRequest) {
        IdentityParams identityParams = IdentityParams.with()
                .password(new TextField(registrationRequest.password))
                .firstname(new TextField(registrationRequest.firstname))
                .lastname(new TextField(registrationRequest.lastname))
                .registrationToken(new TextField(null))
                .expirationDateTime(new DateTimeField(null))
                .state(new EnumerationMapField(IdentityState.ACTIVE.name()))
                .build();

        identity = identityService.encodePasswordAndUpdate(identity, identityParams);

        Optional<User> userOpt = userService.findById(identity.getMainActorId());
        if (userOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Identity [%s] has no default user!", identity.getStringId()));
        }

        UserParams userParams = UserParams.with()
                .firstname(new TextField(registrationRequest.firstname))
                .lastname(new TextField(registrationRequest.lastname))
                .build();
        userService.update(userOpt.get(), userParams);

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
    @Scheduled(cron = "0 0 1 * * *")
    public void resetExpiredToken() {
        log.info("Resetting expired identity tokens");
        List<Identity> identities = identityService.findAllByStateAndExpirationDateBefore(IdentityState.BLOCKED,
                LocalDateTime.now());
        if (identities.isEmpty()) {
            log.info("There are none expired tokens. Everything is awesome.");
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            NaeTransaction transaction = NaeTransaction.builder()
                    .transactionManager(transactionManager)
                    .event(new Closure<>(null) {
                        @Override
                        public List<Identity> call() {
                            return doResetExpiredToken(identities);
                        }
                    })
                    .build();
            transaction.begin();
        } else {
            doResetExpiredToken(identities);
        }

        log.info("Reset {} expired identity tokens", identities.size());
    }

    private List<Identity> doResetExpiredToken(List<Identity> identities) {
        identities.forEach(identity -> {
            identityService.update(identity, IdentityParams.with()
                    .registrationToken(new TextField(null))
                    .expirationDateTime(new DateTimeField(null))
                    .build());
        });
        return identities;
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
