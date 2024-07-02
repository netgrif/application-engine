package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
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

@Slf4j
@Component
@Profile("!test")
@RunnerOrder(1)
@RequiredArgsConstructor
public class MongoDbRunner extends AbstractOrderedApplicationRunner {

    @Value("${spring.data.mongodb.database}")
    private String name;

    @Value("${spring.data.mongodb.host:null}")
    private String host;

    @Value("${spring.data.mongodb.port:null}")
    private String port;

    @Value("${spring.data.mongodb.uri:null}")
    private String uri;

    @Value("${spring.data.mongodb.drop}")
    private boolean dropDatabase;

    @Value("${spring.data.mongodb.runner-ensure-index}")
    private boolean resolveIndexesOnStartup;

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (dropDatabase) {
            if (host != null && port != null)
                log.info("Dropping Mongo database {}:{}/{}", host, port, name);
            else if (uri != null)
                log.info("Dropping Mongo database {}", uri);
            mongoTemplate.getDb().drop();
        }
        if (resolveIndexesOnStartup) {
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
