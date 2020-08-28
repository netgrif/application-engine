package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository
import com.netgrif.workflow.startup.DefaultRoleRunner
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.startup.SystemUserRunner
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
    private UserProcessRoleRepository roleRepository
    @Autowired
    private SystemUserRunner systemUserRunner
    @Autowired
    private DefaultRoleRunner roleRunner
    @Autowired
    private ElasticTaskRepository elasticTaskRepository
    @Autowired
    private ElasticCaseRepository elasticCaseRepository

    void truncateDbs() {
        template.db.drop()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        elasticTaskRepository.deleteAll()
        elasticCaseRepository.deleteAll()
        roleRunner.run()
        superCreator.run()
        systemUserRunner.run()
    }
}