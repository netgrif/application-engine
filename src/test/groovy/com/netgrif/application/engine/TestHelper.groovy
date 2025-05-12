package com.netgrif.application.engine

import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.LoggedIdentity
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.domain.repositories.RoleAssignmentRepository
import com.netgrif.application.engine.authorization.domain.repositories.RoleRepository
import com.netgrif.application.engine.authorization.service.RoleService
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService
import com.netgrif.application.engine.authorization.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticTaskRepository
import com.netgrif.application.engine.petrinet.domain.repositories.UriNodeRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.*
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class TestHelper {

    @Autowired
    private SuperCreator superCreator
    @Autowired
    private MongoTemplate template
    @Autowired
    private RoleRepository roleRepository
    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository
    @Autowired
    private RoleService roleService
    @Autowired
    private SystemUserRunner systemIdentityRunner
    @Autowired
    private SystemProcessRunner systemProcessRunner
    @Autowired
    private ApplicationRoleRunner applicationRoleRunner
    @Autowired
    private ElasticTaskRepository elasticTaskRepository
    @Autowired
    private ElasticCaseRepository elasticCaseRepository
    @Autowired
    private UriNodeRepository uriNodeRepository
    @Autowired
    private IFieldActionsCacheService actionsCacheService
    @Autowired
    private FilterRunner filterRunner
    @Autowired
    private FinisherRunner finisherRunner
    @Autowired
    private UriRunner uriRunner
    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private ValidationRunner validationRunner
    @Autowired
    private DefaultGroupRunner defaultGroupRunner
    @Autowired
    private IUserService userService
    @Autowired
    private IGroupService groupService
    @Autowired
    private IIdentityService identityService;

    void truncateDbs() {
        template.db.drop()
        elasticTaskRepository.deleteAll()
        elasticCaseRepository.deleteAll()
        uriNodeRepository.deleteAll()
        roleAssignmentRepository.deleteAll()
        roleRepository.deleteAll()
        roleService.clearCache()
        actionsCacheService.clearActionCache()
        actionsCacheService.clearFunctionCache()
        actionsCacheService.clearNamespaceFunctionCache()
        petriNetService.evictAllCaches()
        userService.clearForbiddenKeywords()
        groupService.clearForbiddenKeywords()
        identityService.clearForbiddenKeywords()

        uriRunner.run()
        systemProcessRunner.run()
        applicationRoleRunner.run()
        defaultGroupRunner.run()
        systemIdentityRunner.run()
        filterRunner.run()
        superCreator.run()
        validationRunner.run()
        finisherRunner.run()
    }

    static void login(Identity identity) {
        LoggedIdentity loggedTest = identity.toSession()
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loggedTest,
                loggedTest.getPassword(), loggedTest.getAuthorities())
        SecurityContextHolder.getContext().setAuthentication(token)
    }

    static void logout() {
        SecurityContextHolder.getContext().setAuthentication(null)
    }

    static InputStream stream(String resource) {
        return TestHelper.getClassLoader().getResourceAsStream(resource)
    }
}