package com.netgrif.application.engine.startup


import com.netgrif.application.engine.menu.service.interfaces.IMenuItemService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mapping.context.MappingContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.data.mongodb.core.index.IndexResolver
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty
import org.springframework.stereotype.Component

@Component
class MongoDbRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoDbRunner)

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private IMenuItemService menuItemService

    @Value('${spring.data.mongodb.database}')
    private String name

    @Value('${spring.data.mongodb.host:null}')
    private String host

    @Value('${spring.data.mongodb.port:null}')
    private String port

    @Value('${spring.data.mongodb.uri:null}')
    private String uri

    @Value('${spring.data.mongodb.drop}')
    private boolean dropDatabase

    @Value('${spring.data.mongodb.runner-ensure-index}')
    private boolean resolveIndexesOnStartup

    @Override
    void run(String... strings) throws Exception {
        if (dropDatabase) {
            if (host != null && port != null)
                log.info("Dropping Mongo database ${host}:${port}/${name}")
            else if (uri != null)
                log.info("Dropping Mongo database ${uri}")
            mongoTemplate.getDb().drop()
        }
        if (resolveIndexesOnStartup) {
            log.info("Ensuring Mongo indexes")
            resolveIndexes()
        }
    }

    private void resolveIndexes() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext()
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext)
        mappingContext.getPersistentEntities()
                .stream()
                .filter(it -> it.isAnnotationPresent(Document.class))
                .forEach(it -> {
                    IndexOperations indexOps = mongoTemplate.indexOps(it.getType())
                    resolver.resolveIndexFor(it.getType()).forEach(indexOps::ensureIndex)
                })
        menuItemService.ensureDatabaseIndexes()
    }
}