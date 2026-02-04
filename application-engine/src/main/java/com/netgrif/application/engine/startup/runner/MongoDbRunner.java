package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.configuration.MongoIndexesConfigurator;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RunnerOrder(10)
@RequiredArgsConstructor
public class MongoDbRunner implements ApplicationEngineStartupRunner {

    private final DataConfigurationProperties.MongoProperties mongoProperties;

    private final MongoTemplate mongoTemplate;

    private final MongoIndexesConfigurator mongoIndexesConfigurator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (mongoProperties.getDrop()) {
            if (mongoProperties.getHost() != null && mongoProperties.getPort() != null) {
                log.info("Dropping Mongo database {}:{}/{}", mongoProperties.getHost(), mongoProperties.getPort(), mongoProperties.getDatabase());
            } else if (mongoProperties.getUri() != null) {
                log.info("Dropping Mongo database {}", mongoProperties.getUri());
            }
            mongoTemplate.getDb().drop();
            mongoIndexesConfigurator.resolveCollections();
        }
        if (mongoProperties.getRunnerEnsureIndex()) {
            mongoIndexesConfigurator.resolveIndexes();
        }
    }


}
