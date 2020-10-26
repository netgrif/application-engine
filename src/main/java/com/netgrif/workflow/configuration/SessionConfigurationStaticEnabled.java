package com.netgrif.workflow.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@Configuration
@ConditionalOnProperty(
        value = "server.security.static.enabled",
        havingValue = "true"
)
public class SessionConfigurationStaticEnabled {

    @Value("${spring.session.redis.host}")
    private String hostName;

    @Value("${spring.session.redis.port}")
    private Integer port;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        hostName = hostName == null ? "localhost" : hostName;
        port = port == null || port == 0 ? 6379 : port;
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostName, port);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

}
