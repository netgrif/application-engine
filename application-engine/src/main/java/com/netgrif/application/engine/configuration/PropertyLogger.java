package com.netgrif.application.engine.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Component
public class PropertyLogger {

    private static final Logger log = LoggerFactory.getLogger(PropertyLogger.class);
    private static final String[] SECRET_PROPS = new String[]{"secret", "password", "credentials", "heslo", "pass"};

    @EventListener
    public void logProperties(ContextRefreshedEvent event) {
        final Environment env = event.getApplicationContext().getEnvironment();
        log.info("====== Environment and configuration ======");
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));

        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .forEach(prop -> log.info("{}: {}", prop, getSanitizedProperty(prop, env)));
        log.info("===========================================");
    }

    private String getSanitizedProperty(String property, Environment env) {
        String value = env.getProperty(property);
        if (value == null) {
            return "";
        }
        if (Arrays.stream(SECRET_PROPS).anyMatch(s -> property.toLowerCase().endsWith(s))) {
            return value.isEmpty() ? "" : "**********";
        }
        return value;
    }

}
