package com.netgrif.application.engine.mapper.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.configuration.properties.ObjectMapperConfigurationProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class ObjectMapperConfiguration {

    private static final String TYPE_METHOD_NAME = "getOriginalType";

    private final ObjectMapperConfigurationProperties properties;

    public ObjectMapperConfiguration(ObjectMapperConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder
                .createXmlMapper(false)
                .featuresToDisable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .mixIns(createMixInPairs().entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)))
                .build();
    }

    private Map<Class<?>, Class<?>> createMixInPairs() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true, new StandardServletEnvironment());
        provider.addIncludeFilter(new AnnotationTypeFilter(NaeMixin.class, true, true));
        Set<BeanDefinition> beanDefs = provider.findCandidateComponents(properties.getMixinPackage());
        return beanDefs.stream().map(bd -> {
            try {
                return Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(c -> c, c -> {
            try {
                return (Class<?>) c.getMethod(TYPE_METHOD_NAME).invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
