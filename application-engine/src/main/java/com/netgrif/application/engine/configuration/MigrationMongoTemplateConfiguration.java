package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.configuration.properties.MigrationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MigrationMongoTemplateConfiguration {

    private static final String DEFAULT_TEMPLATE = "mongoTemplate";
    private final BeanFactory beanFactory;
    private final MigrationProperties migrationProperties;

    @Bean(name = "migrationMongoTemplate")
    public MongoTemplate getMongoTemplate() {
        String requested = migrationProperties.getMongoTemplateBeanName();

        if (!StringUtils.hasText(requested)) {
            requested = DEFAULT_TEMPLATE;
        } else {
            requested = requested.trim();
        }

        if (!beanFactory.containsBean(requested)) {
            log.warn("Migration MongoTemplate bean [{}] not found, falling back to [{}]", requested, DEFAULT_TEMPLATE);
            return beanFactory.getBean(DEFAULT_TEMPLATE, MongoTemplate.class);
        }

        try {
            MongoTemplate template = beanFactory.getBean(requested, MongoTemplate.class);
            log.info("Using MongoTemplate bean [{}] for migration helpers", requested);
            return template;
        } catch (BeansException e) {
            log.warn("Migration MongoTemplate bean [{}] is not usable, falling back to [{}]",
                    requested, DEFAULT_TEMPLATE, e);
            return beanFactory.getBean(DEFAULT_TEMPLATE, MongoTemplate.class);
        }
    }
}