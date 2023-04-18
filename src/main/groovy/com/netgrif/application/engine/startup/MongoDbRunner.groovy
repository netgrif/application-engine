package com.netgrif.application.engine.startup

import com.netgrif.application.engine.configuration.properties.MongoProperties
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!test")
@CompileStatic
class MongoDbRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private MongoProperties mongoProperties

    @Override
    void run(String... strings) throws Exception {
        if (mongoProperties.drop) {
            if (mongoProperties.host != null && mongoProperties.port != null)
                log.info("Dropping Mongo database ${mongoProperties.host}:${mongoProperties.port}/${mongoProperties.database}")
            else if (mongoProperties.uri != null)
                log.info("Dropping Mongo database ${mongoProperties.uri}")
            mongoTemplate.getDb().drop()
        }
    }
}