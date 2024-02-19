package com.netgrif.application.engine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
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

    @Value("${spring.session.redis.host}")
    private String hostName;

    @Value("${spring.session.redis.port}")
    private Integer port;

    @Value("${spring.session.redis.username:#{null}}")
    private String username;

    @Value("${spring.session.redis.password:#{null}}")
    private String password;


    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        hostName = hostName == null ? "localhost" : hostName;
        port = port == null || port == 0 ? 6379 : port;
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostName, port);
        if(username != null && password !=null && !username.isEmpty() && !password.isEmpty()){
            redisStandaloneConfiguration.setUsername(username);
            redisStandaloneConfiguration.setPassword(password);
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