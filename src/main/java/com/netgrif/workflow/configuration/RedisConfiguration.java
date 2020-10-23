package com.netgrif.workflow.configuration;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(redisNamespace = "spring:session:${spring.session.redis.namespace}")
public class RedisConfiguration {
}
