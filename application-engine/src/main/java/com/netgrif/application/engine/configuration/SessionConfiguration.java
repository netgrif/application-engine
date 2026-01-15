package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@EnableRedisIndexedHttpSession(redisNamespace = "spring:session:${netgrif.engine.data.redis.session.namespace}")
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
        if (hasCredentials(redisProperties.getUsername(), redisProperties.getPassword())) {
            redisStandaloneConfiguration.setUsername(redisProperties.getUsername());
            redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        }
        JedisClientConfiguration clientConfiguration = jedisClientConfiguration();
        return new JedisConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
    }

    protected JedisConnectionFactory redisSentinelConfiguration() {
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        sentinelConfiguration.setMaster(redisProperties.getSentinel().getMaster());
        List<RedisNode> nodes = redisProperties.getSentinel().getNodes().stream().map(node -> {
            try {
                return RedisNode.fromString(node);
            } catch (Exception e) {
                log.warn("Parsing redis sentinel node {} has failed. Trying to use the value as an address without port and adding default sentinel port {}", node, RedisNode.DEFAULT_SENTINEL_PORT, e);
                return new RedisNode(node, RedisNode.DEFAULT_SENTINEL_PORT);
            }
        }).toList();
        sentinelConfiguration.setSentinels(nodes);

        if (hasCredentials(redisProperties.getUsername(), redisProperties.getPassword())) {
            sentinelConfiguration.setUsername(redisProperties.getUsername());
            sentinelConfiguration.setPassword(redisProperties.getPassword());
        }
        if (hasCredentials(redisProperties.getSentinel().getUsername(), redisProperties.getSentinel().getPassword())) {
            sentinelConfiguration.setSentinelUsername(redisProperties.getSentinel().getUsername());
            sentinelConfiguration.setSentinelPassword(redisProperties.getSentinel().getPassword());
        }

        JedisClientConfiguration clientConfiguration = jedisClientConfiguration();
        return new JedisConnectionFactory(sentinelConfiguration, clientConfiguration);
    }

    protected JedisClientConfiguration jedisClientConfiguration() {
        if (redisProperties.isSsl()) {
            return JedisClientConfiguration.builder().useSsl().build();
        }
        return JedisClientConfiguration.defaultConfiguration();
    }

    private boolean hasCredentials(String username, String password) {
        return username != null && !username.isBlank() &&
                password != null && !password.isBlank();
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
