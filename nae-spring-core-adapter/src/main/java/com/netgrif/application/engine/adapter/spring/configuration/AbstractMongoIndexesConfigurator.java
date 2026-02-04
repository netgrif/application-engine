package com.netgrif.application.engine.adapter.spring.configuration;

import com.netgrif.application.engine.objects.annotations.Indexable;
import com.netgrif.application.engine.objects.annotations.Indexed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
public abstract class AbstractMongoIndexesConfigurator {

    private final MongoTemplate mongoTemplate;
    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
    private final IndexResolver resolver;

    public AbstractMongoIndexesConfigurator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.mappingContext = mongoTemplate.getConverter().getMappingContext();
        this.resolver = new MongoPersistentEntityIndexResolver(mappingContext);
    }

    public abstract MultiValueMap<Class<?>, String> getIndexes();
    public abstract List<Class<?>> getEntityIndexBlacklist();

    public void resolveCollections() {
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Indexable.class) && !getEntityIndexBlacklist().contains(it.getType()))
                .forEach(mongoPersistentEntity -> {
                    if (!mongoTemplate.collectionExists(mongoPersistentEntity.getCollection())) {
                        mongoTemplate.createCollection(mongoPersistentEntity.getCollection());
                    }
                });
    }

    public void resolveIndexes() {
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Indexable.class) && !getEntityIndexBlacklist().contains(it.getType()))
                .forEach(mongoPersistentEntity -> resolveIndexes(mongoPersistentEntity.getCollection(), mongoPersistentEntity.getType()));
    }

    public void resolveIndexes(String collectionName, Class<?> collectionType) {
        IndexOperations indexOps = mongoTemplate.indexOps(collectionName);
        log.info("Ensuring existence of indexes for {}", collectionType.getSimpleName());
        List<IndexDefinition> indexDefinitions = new ArrayList<>();
        Document document = collectionType.getAnnotation(Document.class);
        if (document != null) {
            indexDefinitions.addAll(StreamSupport.stream(resolver.resolveIndexFor(collectionType).spliterator(), false).toList());
        }
        addAnnotatedFields(collectionType, indexDefinitions);
        addConfiguredFields(collectionType, indexDefinitions);
        indexDefinitions.forEach(indexOps::createIndex);
    }

    private void addAnnotatedFields(Class<?> collectionType, List<IndexDefinition> indexDefinitions) {
        Iterable<Field> additionalIndexedProperties = FieldUtils.getFieldsListWithAnnotation(collectionType, Indexed.class);;
        additionalIndexedProperties.forEach(property -> {
            String fieldName = property.getName();
            org.springframework.data.mongodb.core.mapping.Field markedField = property.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
            if (markedField != null) {
                fieldName = markedField.value();
            }
            indexDefinitions.add(new Index().on(fieldName, Sort.Direction.ASC).named(fieldName));
        });
    }


    private void addConfiguredFields(Class<?> collectionType, List<IndexDefinition> indexDefinitions) {
        if (getIndexes().containsKey(collectionType)) {
            getIndexes().get(collectionType).forEach(indexableFieldName -> {
                indexDefinitions.add(new Index().on(indexableFieldName, Sort.Direction.ASC).named(indexableFieldName));
            });
        }
    }
}
