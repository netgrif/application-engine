package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
@EnableRedisHttpSession(redisNamespace = "spring:session:${spring.session.redis.namespace}")
@ConditionalOnProperty(
        value = "nae.server.security.static.enabled",
        havingValue = "false"
)
public class SessionConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        String hostName = redisProperties.getHost() == null ? "localhost" : redisProperties.getHost();
        Integer port = redisProperties.getPort() == null || redisProperties.getPort() == 0 ? 6379 : redisProperties.getPort();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostName, port);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }


    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

}