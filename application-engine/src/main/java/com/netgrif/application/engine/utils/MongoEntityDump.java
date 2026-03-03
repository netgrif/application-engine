package com.netgrif.application.engine.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class MongoEntityDump {

    private final MappingMongoConverter mongoConverter;

    public MongoEntityDump(MappingMongoConverter mongoConverter) {
        this.mongoConverter = mongoConverter;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dumpMongoEntities() {
        if (!log.isDebugEnabled() && !log.isTraceEnabled()) {
            return;
        }

        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoConverter.getMappingContext();

        List<? extends MongoPersistentEntity<?>> entities = new ArrayList<>(mappingContext.getPersistentEntities());
        entities.sort(Comparator.comparing(entity -> entity.getType().getName()));

        log.debug("Mongo MappingContext persistent entities: {}", entities.size());

        if (log.isTraceEnabled()) {
            for (MongoPersistentEntity<?> entity : entities) {
                String typeName = entity.getType().getName();
                String collection = entity.getCollection();
                log.trace("MongoEntity: {} | collection={}", typeName, collection);
            }
        }
    }
}