package com.netgrif.application.engine.startup

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!test")
@CompileStatic
class FlushSessionsRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private JedisConnectionFactory connectionFactory

    @Autowired
    private RedisIndexedSessionRepository repository


    @Override
    void run(String... args) {
        log.info("Flushing all users session")
        connectionFactory.connection.flushAll()
        repository.cleanupExpiredSessions()
    }
}