package com.netgrif.application.engine

import com.netgrif.application.engine.auth.domain.repositories.UserRepository
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticTaskRepository
import com.netgrif.application.engine.petrinet.domain.repositories.UriNodeRepository
import com.netgrif.application.engine.authorization.domain.repositories.ProcessRoleRepository
import com.netgrif.application.engine.authorization.service.RoleService
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
    private UserRepository userRepository
    @Autowired
    private ProcessRoleRepository roleRepository
    @Autowired
    private RoleService roleService
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
    private IPetriNetService petriNetService
    @Autowired
    private ValidationRunner validationRunner

    void truncateDbs() {
        template.db.drop()
        elasticTaskRepository.deleteAll()
        elasticCaseRepository.deleteAll()
        uriNodeRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleService.clearCache()
        actionsCacheService.clearActionCache()
        actionsCacheService.clearFunctionCache()
        actionsCacheService.clearNamespaceFunctionCache()
        petriNetService.evictAllCaches()

        defaultRoleRunner.run()
        anonymousRoleRunner.run()
        systemUserRunner.run()
        uriRunner.run()
        groupRunner.run()
        filterRunner.run()
        impersonationRunner.run()
        superCreator.run()
        validationRunner.run()
        finisherRunner.run()
    }

    static InputStream stream(String resource) {
        return TestHelper.getClassLoader().getResourceAsStream(resource)
    }
}