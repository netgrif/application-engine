package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
                    List<IndexDefinition> indexDefinitions = StreamSupport.stream(resolver.resolveIndexFor(it.getType()).spliterator(), false).collect(Collectors.toList());
                    addAnnotatedFields(it, indexDefinitions);
                    addConfiguredFields(it, indexDefinitions);
                    indexDefinitions.forEach(indexOps::ensureIndex);
                });
    }

    private void addAnnotatedFields(MongoPersistentEntity<?> mongoPersistentEntity, List<IndexDefinition> indexDefinitions) {
        Iterable<MongoPersistentProperty> additionalIndexedProperties = mongoPersistentEntity.getPersistentProperties(Indexed.class);
        additionalIndexedProperties.forEach(property -> {
            indexDefinitions.add(new Index().on(property.getFieldName(), Sort.Direction.ASC).named(property.getFieldName()));
        });
    }

    private void addConfiguredFields(MongoPersistentEntity<?> mongoPersistentEntity, List<IndexDefinition> indexDefinitions) {
        if (mongoProperties.getIndices().containsKey(mongoPersistentEntity.getType())) {
            mongoProperties.getIndices().get(mongoPersistentEntity.getType()).forEach(indexableFieldName -> {
                indexDefinitions.add(new Index().on(indexableFieldName, Sort.Direction.ASC).named(indexableFieldName));
            });
        }
    }
}
