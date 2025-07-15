package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import static org.bouncycastle.cms.RecipientId.password;

@Configuration
@EnableRedisIndexedHttpSession(redisNamespace = "spring:session:${netgrif.engine.data.redis.namespace}")
@ConditionalOnProperty(
        value = "netgrif.engine.security.static.enabled",
        havingValue = "false",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class SessionConfiguration {

    private final DataConfigurationProperties.RedisProperties redisProperties;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        String hostName = redisProperties.getHost() == null ? "localhost" : redisProperties.getHost();
        int port = redisProperties.getPort() == 0 ? 6379 : redisProperties.getPort();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostName, port);
        if(redisProperties.getUsername() != null && redisProperties.getPassword() !=null && !redisProperties.getUsername().isEmpty() && !redisProperties.getPassword().isEmpty()){
            redisStandaloneConfiguration.setUsername(redisProperties.getUsername());
            redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        }
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}