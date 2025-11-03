package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.List;

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
        if (redisProperties.getSentinel().getMaster() != null && !redisProperties.getSentinel().getMaster().isEmpty()) {
            return redisSentinelConfiguration();

        } else {
            return standaloneRedisConfiguration();
        }
    }

    protected JedisConnectionFactory standaloneRedisConfiguration() {
        String hostName = redisProperties.getHost() == null ? "localhost" : redisProperties.getHost();
        int port = redisProperties.getPort() == 0 ? RedisNode.DEFAULT_PORT : redisProperties.getPort();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostName, port);
        if (redisProperties.getUsername() != null && redisProperties.getPassword() != null && !redisProperties.getUsername().isEmpty() && !redisProperties.getPassword().isEmpty()) {
            redisStandaloneConfiguration.setUsername(redisProperties.getUsername());
            redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        }
        JedisClientConfiguration clientConfiguration = jedisClientConfiguration();
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    protected JedisConnectionFactory redisSentinelConfiguration() {
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        sentinelConfiguration.setMaster(redisProperties.getSentinel().getMaster());
        List<RedisNode> nodes = redisProperties.getSentinel().getNodes().stream().map(node -> {
            String nodeAddress = node;
            if (!nodeAddress.contains(":")) {
                nodeAddress += ":" + RedisNode.DEFAULT_SENTINEL_PORT;
            }
            return RedisNode.fromString(nodeAddress);
        }).toList();
        sentinelConfiguration.setSentinels(nodes);

        if (redisProperties.getUsername() != null && redisProperties.getPassword() != null &&
                !redisProperties.getUsername().isEmpty() && !redisProperties.getPassword().isEmpty()) {
            sentinelConfiguration.setUsername(redisProperties.getUsername());
            sentinelConfiguration.setPassword(redisProperties.getPassword());
        }
        if (redisProperties.getSentinel().getUsername() != null && redisProperties.getSentinel().getPassword() != null &&
                !redisProperties.getSentinel().getUsername().isEmpty() && !redisProperties.getSentinel().getPassword().isEmpty()) {
            sentinelConfiguration.setSentinelUsername(redisProperties.getSentinel().getUsername());
            sentinelConfiguration.setSentinelPassword(redisProperties.getSentinel().getPassword());
        }

        return new JedisConnectionFactory(sentinelConfiguration);
    }

    protected JedisClientConfiguration jedisClientConfiguration() {
        if (redisProperties.isSsl()) {
            return JedisClientConfiguration.builder().useSsl().build();
        }
        return JedisClientConfiguration.defaultConfiguration();
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
