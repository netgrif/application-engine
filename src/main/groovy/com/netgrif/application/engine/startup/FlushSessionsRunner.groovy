package com.netgrif.application.engine.startup

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class FlushSessionsRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FlushSessionsRunner.class.name)

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