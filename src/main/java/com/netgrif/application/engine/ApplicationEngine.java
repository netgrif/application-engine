package com.netgrif.application.engine;

import com.netgrif.application.engine.workflow.domain.version.StringToVersionConverter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import java.util.ArrayList;
import java.util.List;

@EnableCaching
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableMongoAuditing
@Aspect
@Slf4j
public class ApplicationEngine {

    @Around("execution(* com.netgrif.application.engine.startup.AbstractOrderedCommandLineRunner+.run(..))")
    void logRun(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("{} started", joinPoint.getTarget().getClass().getSimpleName());
        joinPoint.proceed();
        log.info("{} finished", joinPoint.getTarget().getClass().getSimpleName());
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new StringToVersionConverter());
        return new MongoCustomConversions(converters);
    }

    public static void main(String[] args) {
        SpringApplication.run(ApplicationEngine.class, args);
    }
}