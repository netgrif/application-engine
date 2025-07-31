package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.adapter.spring.configuration.AbstractMongoIndexesConfigurator;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
public class MongoIndexesConfigurator extends AbstractMongoIndexesConfigurator {

    private final DataConfigurationProperties.MongoProperties mongoProperties;

    public MongoIndexesConfigurator(MongoTemplate mongoTemplate,
                                    DataConfigurationProperties.MongoProperties mongoProperties) {
        super(mongoTemplate);
        this.mongoProperties = mongoProperties;
    }

    @Override
    public MultiValueMap<Class<?>, String> getIndexes() {
        return mongoProperties.getIndexes();
    }
}
