package com.netgrif.application.engine.event.events;

import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class GlobalEventChainConfiguration {


    @Bean
    @Autowired
    public EventChain GlobalEventChain(ApplicationContext applicationContext) {
       Set<Class<?>> allowedClasses = applicationContext.getBeansOfType(AbstractDispatcher.class).
                values().stream().map(AbstractDispatcher::getClass).collect(Collectors.toSet());
        EventChain globalChain = new EventChain(allowedClasses);
        globalChain.setLength(100); // todo properties
        return globalChain;
    }
}
