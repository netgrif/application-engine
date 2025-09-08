package com.netgrif.application.engine.manager.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import com.netgrif.application.engine.security.service.SecurityContextService;
import com.netgrif.application.engine.startup.SystemUserRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SessionManagerService implements ISessionManagerService {

    protected final RedisIndexedSessionRepository repository;
    protected final SessionRegistry sessionRegistry;
    protected final SecurityContextService securityContextService;
    protected final SystemUserRunner systemUserRunner;
    public static LoggedIdentity systemIdentity = null;

    protected final String redisUsernameKey;

    public SessionManagerService(RedisIndexedSessionRepository repository, SessionRegistry sessionRegistry,
                                 SecurityContextService securityContextService, SystemUserRunner systemUserRunner,
                                 @Value("${spring.session.redis.namespace}") String redisNamespace) {
        this.repository = repository;
        this.sessionRegistry = sessionRegistry;
        this.securityContextService = securityContextService;
        this.systemUserRunner = systemUserRunner;
        this.redisUsernameKey = RedisIndexedSessionRepository.DEFAULT_NAMESPACE + ":" + redisNamespace
                + ":index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:";
    }

    /**
     * Gets currently logged identity
     *
     * @return Currently logged identity. Can be null if nobody is logged in.
     */
    @Override
    public LoggedIdentity getLoggedIdentity() {
        if (!securityContextService.isAuthenticatedPrincipalLoggedIdentity()) {
            return getSystemIdentity();
        }
        return (LoggedIdentity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public LoggedIdentity getSystemIdentity() {
        if (systemIdentity != null) {
            return systemIdentity;
        }
        if (systemUserRunner == null || systemUserRunner.getSystemUser() == null) {
            return null;
        }
        User systemUser = systemUserRunner.getSystemUser();
        systemIdentity = LoggedIdentity.with()
                .fullName(systemUser.getFullName())
                .identityId(systemUser.getStringId())
                .activeActorId(systemUser.getStringId())
                .username(systemUser.getEmail())
                .password("")
                .build();
        return systemIdentity;
    }

    /**
     * Gets id of currently selected actor of logged identity
     *
     * @return The id of the selected actor if any identity is logged in. Can be null.
     */
    @Override
    public String getActiveActorId() {
        LoggedIdentity loggedIdentity = getLoggedIdentity();
        if (loggedIdentity != null) {
            return loggedIdentity.getActiveActorId();
        }
        return null;
    }

    @Override
    public List<LoggedIdentity> getAllLoggedIdentities() {
        RedisOperations<Object, Object> redisOps = repository.getSessionRedisOperations();
        if (redisOps == null) {
            throw new IllegalStateException("Redis session management is not configured!");
        }
        List<LoggedIdentity> activeIdentities = new ArrayList<>();
        Set<Object> keys = redisOps.keys(redisUsernameKey + "*");
        if (keys == null || keys.isEmpty()) {
            return activeIdentities;
        }
        keys.forEach(username -> {
            Session session = repository.findByPrincipalName(username.toString().replace(redisUsernameKey, ""))
                    .values().stream().findFirst().orElse(null);
            if (session != null) {
                SecurityContextImpl impl = session.getAttribute(WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
                if (impl != null) {
                    LoggedIdentity identity = (LoggedIdentity) impl.getAuthentication().getPrincipal();
                    if (identity != null) {
                        activeIdentities.add(identity);
                    }
                }
            }
        });
        return activeIdentities;
    }

    @Override
    public void logoutSessionByUsername(String username) {
        repository.findByPrincipalName(username).keySet().forEach(repository::deleteById);
    }

    @Override
    public void logoutAllSession() {
        List<LoggedIdentity> loggedIdentities = getAllLoggedIdentities();
        loggedIdentities.forEach(loggedUser -> repository.findByPrincipalName(loggedUser.getUsername())
                .keySet().forEach(repository::deleteById));
    }

}
