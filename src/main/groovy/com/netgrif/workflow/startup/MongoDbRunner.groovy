package com.netgrif.workflow.startup

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class MongoDbRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(MongoDbRunner)

    @Autowired
    private MongoTemplate mongoTemplate

    @Value('${spring.data.mongodb.database}')
    private String name

    @Value('${spring.data.mongodb.host}')
    private String host

    @Value('${spring.data.mongodb.port}')
    private String port

    @Value('${spring.data.mongodb.drop}')
    private boolean dropDatabase

    @Override
    void run(String... strings) throws Exception {
        if (dropDatabase) {
            log.info("Dropping Mongo database ${host}:${port}/${name}")
            mongoTemplate.getDb().dropDatabase()
        }
    }
}