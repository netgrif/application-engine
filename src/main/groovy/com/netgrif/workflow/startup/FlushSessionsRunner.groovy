package com.netgrif.workflow.startup


import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.session.data.redis.RedisOperationsSessionRepository
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class FlushSessionsRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(FlushSessionsRunner.class.name)

    @Autowired
    private JedisConnectionFactory connectionFactory

    @Override
    void run(String... args) {
        log.info("Flushing all users session")
        RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(connectionFactory)
        connectionFactory.connection.flushAll()
        sessionRepository.cleanupExpiredSessions()
    }
}