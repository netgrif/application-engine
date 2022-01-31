package com.netgrif.application.engine.startup

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class MongoDbRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoDbRunner)

    @Autowired
    private MongoTemplate mongoTemplate

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

    @Override
    void run(String... strings) throws Exception {
        if (dropDatabase) {
            if (host != null && port != null)
                log.info("Dropping Mongo database ${host}:${port}/${name}")
            else if (uri != null)
                log.info("Droppiung Mongo database ${uri}")
            mongoTemplate.getDb().drop()
        }
    }
}