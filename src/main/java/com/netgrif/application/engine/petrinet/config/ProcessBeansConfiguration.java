package com.netgrif.application.engine.petrinet.config;

import com.netgrif.adapter.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.RoleActionsRunner;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.auth.service.UserService;
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
                                                 ISecurityContextService securityContextService) {
        return new com.netgrif.application.engine.petrinet.service.ProcessRoleService(
                processRoleRepository,
                petriNetRepository,
                publisher,
                roleActionsRunner,
                petriNetService,
                userService,
                securityContextService
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public IPetriNetService petriNetService() {
        return new PetriNetService();
    }

}
