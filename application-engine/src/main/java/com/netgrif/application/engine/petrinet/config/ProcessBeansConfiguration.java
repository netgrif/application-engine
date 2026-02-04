package com.netgrif.application.engine.petrinet.config;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import com.netgrif.application.engine.petrinet.service.ProcessRoleServiceImpl;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class ProcessBeansConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProcessRoleService processRoleService(ProcessRoleRepository processRoleRepository,
                                                 PetriNetRepository petriNetRepository,
                                                 ApplicationEventPublisher publisher,
                                                 RoleActionsRunner roleActionsRunner,
                                                 @Lazy PetriNetService petriNetService,
                                                 @Lazy UserService userService,
                                                 ISecurityContextService securityContextService,
                                                 @Lazy GroupService groupService,
                                                 @Lazy RealmService realmService,
                                                 @Lazy PaginationProperties paginationProperties,
                                                 @Lazy IWorkflowService workflowService,
                                                 @Lazy ITaskService taskService
                                                 ) {
        return new ProcessRoleServiceImpl(
                processRoleRepository,
                petriNetRepository,
                publisher,
                roleActionsRunner,
                petriNetService,
                userService,
                securityContextService,
                groupService,
                realmService,
                paginationProperties,
                workflowService,
                taskService
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public IPetriNetService petriNetService() {
        return new PetriNetService();
    }

}
