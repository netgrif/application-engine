package com.netgrif.application.engine

import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticTask
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.ElasticIndexService
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.*
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class TestHelper {

    private static final Object DB_RESET_LOCK = new Object()
    private static final int MONGO_READY_ATTEMPTS = 150
    private static final long MONGO_READY_DELAY_MS = 200L

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private MongoTemplate mongoTemplate

    @Autowired
    private ElasticIndexService indexService

    @Autowired
    private UserService userService

    @Autowired
    private ProcessRoleRepository roleRepository

    @Autowired
    private ProcessRoleService roleService

    @Autowired
    private SystemUserRunner systemUserRunner

    @Autowired
    private DefaultRoleRunner defaultRoleRunner

    @Autowired
    private AnonymousRoleRunner anonymousRoleRunner

    @Autowired
    private ElasticTaskRepository elasticTaskRepository

    @Autowired
    private ElasticCaseRepository elasticCaseRepository

    @Autowired
    private UriNodeRepository uriNodeRepository

    @Autowired
    private GroupRunner groupRunner

    @Autowired
    private IFieldActionsCacheService actionsCacheService

    @Autowired
    private FilterRunner filterRunner

    @Autowired
    private FinisherRunner finisherRunner

    @Autowired
    private MenuProcessRunner menuProcessRunner

    @Autowired
    private ImpersonationRunner impersonationRunner

    @Autowired
    private ElasticsearchRunner elasticsearchRunner

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private DefaultRealmRunner defaultRealmRunner

    void truncateDbs() {
        synchronized (DB_RESET_LOCK) {
            clearMongoCollections()
            indexService.deleteIndex(ElasticPetriNet.class)
            indexService.deleteIndex(ElasticCase.class)
            indexService.deleteIndex(ElasticTask.class)
            roleService.clearCache()
            actionsCacheService.clearActionCache()
            actionsCacheService.clearFunctionCache()
            actionsCacheService.clearGlobalFunctionCache()
            petriNetService.evictAllCaches()

        defaultRoleRunner.run()
        anonymousRoleRunner.run()
        elasticsearchRunner.run()
        defaultRealmRunner.run()
        systemUserRunner.run()
        groupRunner.run()
        filterRunner.run()
        menuProcessRunner.run()
        impersonationRunner.run()
        superCreator.run()
        finisherRunner.run()
    }
}

    private void clearMongoCollections() {
        int attempts = 0
        while (true) {
            try {
                List<String> collections = mongoCollections()
                collections.each { mongoTemplate.dropCollection(it) }
                List<String> remainingCollections = mongoCollections()
                if (!remainingCollections.isEmpty()) {
                    if (++attempts >= MONGO_READY_ATTEMPTS) {
                        throw new IllegalStateException("Mongo database still contains collections after cleanup: ${remainingCollections}")
                    }
                    Thread.sleep(MONGO_READY_DELAY_MS)
                    continue
                }
                return
            } catch (Exception e) {
                if (!isDatabaseDropPending(e) || ++attempts >= MONGO_READY_ATTEMPTS) {
                    throw e
                }
                Thread.sleep(MONGO_READY_DELAY_MS)
            }
        }
    }

    private List<String> mongoCollections() {
        return mongoTemplate.db.listCollectionNames()
                .into(new ArrayList<String>())
                .findAll { !it.startsWith("system.") }
    }

    private static boolean isDatabaseDropPending(Throwable throwable) {
        Throwable current = throwable
        while (current != null) {
            if (current instanceof com.mongodb.MongoCommandException && current.errorCode == 215) {
                return true
            }
            if (current instanceof com.mongodb.MongoWriteException && current.error?.code == 215) {
                return true
            }
            if (current.message?.contains("DatabaseDropPending")
                    || current.message?.contains("database is in the process of being dropped")) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
