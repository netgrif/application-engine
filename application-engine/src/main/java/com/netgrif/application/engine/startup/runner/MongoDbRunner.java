package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.stereotype.Component;

import static org.eclipse.jdt.internal.compiler.parser.Parser.name;

@Slf4j
@Component
@Profile("!test")
@RunnerOrder(10)
@RequiredArgsConstructor
public class MongoDbRunner implements ApplicationEngineStartupRunner {

    private final DataConfigurationProperties.MongoProperties mongoProperties;

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (mongoProperties.getDrop()) {
            if (mongoProperties.getHost() != null && mongoProperties.getPort() != null)
                log.info("Dropping Mongo database {}:{}/{}", mongoProperties.getHost(), mongoProperties.getPort(), name);
            else if (mongoProperties.getUri() != null)
                log.info("Dropping Mongo database {}", mongoProperties.getUri());
            mongoTemplate.getDb().drop();
        }
        if (mongoProperties.getRunnerEnsureIndex()) {
            resolveIndexes();
        }
    }

    void resolveIndexes() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Document.class))
                .forEach(it -> {
                    IndexOperations indexOps = mongoTemplate.indexOps(it.getType());
                    log.info("Ensuring existence of indexes for {}", it.getType().getSimpleName());
                    resolver.resolveIndexFor(it.getType()).forEach(indexOps::ensureIndex);
                });
    }
}
