package com.netgrif.application.engine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(redisNamespace = "spring:session:${spring.session.redis.namespace}")
@ConditionalOnProperty(
        value = "nae.server.security.static.enabled",
        havingValue = "true"
)
public class SessionConfigurationStaticEnabled {

    @Value("${spring.session.redis.host}")
    private String hostName;

    @Value("${spring.session.redis.port}")
    private Integer port;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

}
