package com.netgrif.application.engine.manager.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.manager.service.interfaces.ISessionManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SessionManagerService implements ISessionManagerService {

    protected final RedisIndexedSessionRepository repository;
    protected final SessionRegistry sessionRegistry;

    protected final String redisUsernameKey;

    public SessionManagerService(RedisIndexedSessionRepository repository, SessionRegistry sessionRegistry, @Value("${spring.session.redis.namespace}") String redisNamespace) {
        this.repository = repository;
        this.sessionRegistry = sessionRegistry;
        this.redisUsernameKey = RedisIndexedSessionRepository.DEFAULT_NAMESPACE + ":" + redisNamespace + ":index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:";
    }

    @Override
    public List<LoggedUser> getAllLoggedUsers() {
        RedisOperations<Object, Object> redisOps = repository.getSessionRedisOperations();
        if (redisOps == null) {
            throw new IllegalStateException("Redis session management is not configured!");
        }
        List<LoggedUser> activeUsers = new ArrayList<>();
        Set<Object> keys = redisOps.keys(redisUsernameKey + "*");
        if (keys == null || keys.isEmpty()) {
            return activeUsers;
        }
        keys.forEach(username -> {
            Session session = repository.findByPrincipalName(username.toString().replace(redisUsernameKey, "")).values().stream().findFirst().orElse(null);
            if (session != null) {
                SecurityContextImpl impl = (SecurityContextImpl) session.getAttribute(WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME);
                if (impl != null) {
                    LoggedUser user = (LoggedUser) impl.getAuthentication().getPrincipal();
                    if (user != null) {
                        activeUsers.add(user);
                    }
                }
            }
        });
        return activeUsers;
    }

    @Override
    public void logoutSessionByUsername(String username) {
        repository.findByPrincipalName(username).keySet().forEach(repository::deleteById);
    }

    @Override
    public void logoutAllSession() {
        List<LoggedUser> users = getAllLoggedUsers();
        users.forEach(loggedUser -> repository.findByPrincipalName(loggedUser.getEmail()).keySet().forEach(repository::deleteById));
    }

}
