package com.netgrif.application.engine.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableRedisHttpSession(redisNamespace = "spring:session:${spring.session.redis.namespace}")
@ConditionalOnProperty(
        value = "nae.server.security.static.enabled",
        havingValue = "false"
)
public class SessionConfiguration {

    @Value("${spring.session.redis.host}")
    private String hostName;

    @Value("${spring.session.redis.port}")
    private Integer port;

    @Value("${spring.session.redis.username:#{null}}")
    private String username;

    @Value("${spring.session.redis.password:#{null}}")
    private String password;

    @Value("${spring.session.redis.ssl:#{null}}")
    private Boolean ssl;

    @Value("${spring.redis.sentinel.master:#{null}}")
    private String sentinelMasterName;

    @Value("${spring.redis.sentinel.nodes:#{null}}")
    private List<String> sentinelNodes;

    @Value("${spring.redis.sentinel.port:#{null}}")
    private Integer sentinelPort = 26379;

    @Value("${spring.redis.sentinel.username:#{null}}")
    private String sentinelUsername;

    @Value("${spring.redis.sentinel.password:#{null}}")
    private String sentinelPassword;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        if (sentinelMasterName != null && !sentinelMasterName.isEmpty()) {
            return redisSentinelConfiguration();
        } else {
            return standaloneRedisConfiguration();
        }
    }

    protected JedisConnectionFactory standaloneRedisConfiguration() {
        String hostName = this.hostName == null ? "localhost" : this.hostName;
        int port = this.port == 0 ? 6379 : this.port;
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostName, port);
        if (hasCredentials(username, password)) {
            redisStandaloneConfiguration.setUsername(username);
            redisStandaloneConfiguration.setPassword(password);
        }
        JedisClientConfiguration clientConfiguration = jedisClientConfiguration();
        return new JedisConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
    }

    protected JedisConnectionFactory redisSentinelConfiguration() {
        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration();
        sentinelConfiguration.setMaster(sentinelMasterName);
        List<RedisNode> nodes = sentinelNodes.stream().map(node -> {
            try {
                return RedisNode.fromString(node);
            } catch (Exception e) {
                log.warn("Parsing redis sentinel node {} has failed. Trying to use the value as an address without port and adding default sentinel port {}", node, sentinelPort, e);
                return new RedisNode(node, sentinelPort);
            }
        }).collect(Collectors.toList());
        sentinelConfiguration.setSentinels(nodes);

        if (hasCredentials(username, password)) {
            sentinelConfiguration.setUsername(username);
            sentinelConfiguration.setPassword(password);
        }
        if (hasCredentials(sentinelUsername, sentinelPassword)) {
            sentinelConfiguration.setSentinelUsername(sentinelUsername);
            sentinelConfiguration.setSentinelPassword(sentinelPassword);
        }

        JedisClientConfiguration clientConfiguration = jedisClientConfiguration();
        return new JedisConnectionFactory(sentinelConfiguration, clientConfiguration);
    }

    protected JedisClientConfiguration jedisClientConfiguration() {
        if (ssl) {
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
