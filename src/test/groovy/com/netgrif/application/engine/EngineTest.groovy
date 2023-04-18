//file:noinspection GrMethodMayBeStatic
package com.netgrif.application.engine

import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository
import com.netgrif.application.engine.auth.domain.repositories.UserRepository
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.importer.service.AllDataConfiguration
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.startup.*
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
abstract class EngineTest {

    /**
    Helpers
     */
    @Autowired
    ImportHelper importHelper
    @Autowired
    SuperCreator superCreator
    @Autowired
    ActionDelegate actionDelegate

    /**
    Runners
     */
    @Autowired
    SystemUserRunner systemUserRunner
    @Autowired
    DefaultRoleRunner defaultRoleRunner
    @Autowired
    AnonymousRoleRunner anonymousRoleRunner
    @Autowired
    GroupRunner groupRunner
    @Autowired
    FilterRunner filterRunner
    @Autowired
    FinisherRunner finisherRunner
    @Autowired
    ImpersonationRunner impersonationRunner

    /**
     * Services
     */
    @Autowired
    IPetriNetService petriNetService
    @Autowired
    IWorkflowService workflowService
    @Autowired
    ITaskService taskService
    @Autowired
    IDataService dataService
    @Autowired
    IUserService userService
    @Autowired
    IAuthorityService authorityService
    @Autowired
    ProcessRoleService processRoleService
    @Autowired
    IFieldActionsCacheService actionsCacheService
    @Autowired
    IFilterImportExportService importExportService
    @Autowired
    FieldActionsRunner fieldActionsRunner
    @Autowired
    INextGroupService nextGroupService
    @Autowired
    IUriService uriService
    @Autowired
    IElasticCaseService elasticCaseService

    /**
     * Repositories
     */
    @Autowired
    MongoTemplate template
    @Autowired
    CaseRepository caseRepository
    @Autowired
    TaskRepository taskRepository
    @Autowired
    UserRepository userRepository
    @Autowired
    ProcessRoleRepository roleRepository
    @Autowired
    ElasticTaskRepository elasticTaskRepository
    @Autowired
    ElasticCaseRepository elasticCaseRepository
    @Autowired
    UriNodeRepository uriNodeRepository
    @Autowired
    ProcessRoleRepository processRoleRepository
    @Autowired
    AuthorityRepository authorityRepository

    /**
     * Configurations
     */
    @Autowired
    SuperAdminConfiguration superAdminConfiguration
    @Autowired
    AllDataConfiguration allDataConfiguration

    void truncateDbs() {
        template.db.drop()
        elasticTaskRepository.deleteAll()
        elasticCaseRepository.deleteAll()
        uriNodeRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        processRoleService.clearCache()
        actionsCacheService.clearActionCache()
        actionsCacheService.clearFunctionCache()
        actionsCacheService.clearNamespaceFunctionCache()
        petriNetService.evictAllCaches()

        defaultRoleRunner.run()
        anonymousRoleRunner.run()
        systemUserRunner.run()
        groupRunner.run()
        filterRunner.run()
        impersonationRunner.run()
        superCreator.run()
        finisherRunner.run()
    }

    InputStream stream(String path) {
        return EngineTest.getClassLoader().getResourceAsStream(path)
    }
}