package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(16)
@Profile("!test")
@RequiredArgsConstructor
public class FlushSessionsRunner extends AbstractOrderedApplicationRunner {

    private final JedisConnectionFactory connectionFactory;
    private final RedisIndexedSessionRepository repository;

    @Override
    public void apply(ApplicationArguments args) {
        log.info("Flushing all users session");
        connectionFactory.getConnection().flushAll();
        repository.cleanUpExpiredSessions();
    }

}
