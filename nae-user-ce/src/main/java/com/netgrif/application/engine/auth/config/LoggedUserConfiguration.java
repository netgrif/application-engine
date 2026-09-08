package com.netgrif.application.engine.auth.config;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.auth.service.DefaultLoggedUserFactory;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoggedUserConfiguration {

    private final GroupService groupService;
    private final ProcessRoleService processRoleService;
    private final AuthorityService authorityService;

    @PostConstruct
    public void initializeLoggedUserFactory() {
        ActorTransformer.setLoggedUserFactory(new DefaultLoggedUserFactory(groupService, processRoleService, authorityService));
    }
}
