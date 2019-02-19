package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
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

    void truncateDbs() {
        template.db.dropDatabase()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleRunner.run()
        superCreator.run()
        systemUserRunner.run()
    }
}