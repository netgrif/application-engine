package com.netgrif.application.engine

import com.netgrif.application.engine.auth.domain.repositories.UserRepository
import com.netgrif.application.engine.elastic.domain.ElasticCase
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet
import com.netgrif.application.engine.elastic.domain.ElasticTask
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.ElasticIndexService
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.*
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class TestHelper {

    @Autowired
    private SuperCreator superCreator
    @Autowired
    private MongoTemplate template
    @Autowired
    private ElasticIndexService indexService
    @Autowired
    private UserRepository userRepository
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
    private ImpersonationRunner impersonationRunner
    @Autowired
    private UriRunner uriRunner
    @Autowired
    private ElasticsearchRunner elasticsearchRunner
    @Autowired
    private IPetriNetService petriNetService

    void truncateDbs() {
        template.db.drop()
        indexService.deleteIndex(ElasticPetriNet.class)
        indexService.deleteIndex(ElasticCase.class)
        indexService.deleteIndex(ElasticTask.class)
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleService.clearCache()
        actionsCacheService.clearActionCache()
        actionsCacheService.clearFunctionCache()
        actionsCacheService.clearNamespaceFunctionCache()
        petriNetService.evictAllCaches()

        defaultRoleRunner.run()
        elasticsearchRunner.run()
        anonymousRoleRunner.run()
        systemUserRunner.run()
        uriRunner.run()
        groupRunner.run()
        filterRunner.run()
        impersonationRunner.run()
        superCreator.run()
        finisherRunner.run()
    }
}