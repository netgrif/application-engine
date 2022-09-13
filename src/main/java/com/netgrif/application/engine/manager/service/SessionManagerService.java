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
import java.util.stream.Collectors;

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
        Set<String> keys = redisOps.keys(redisUsernameKey + "*")
                .stream().map(key -> ((String) key).replace(redisUsernameKey, "")).collect(Collectors.toSet());
        List<LoggedUser> activeUsers = new ArrayList<>();
        keys.forEach(username -> {
            Session session = repository.findByPrincipalName(username).values().stream().findFirst().orElse(null);
            if (session != null) {
                System.out.println(session.getId());
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
    public boolean logoutSession(String username) {

        sessionRegistry.removeSessionInformation(username);

        return true;
    }

    @Override
    public boolean logoutAllSession() {

        return true;
    }

}
