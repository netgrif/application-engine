package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.AbstractActor;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMongoEventListener extends AbstractMongoEventListener<AbstractActor> {

    private final ProcessRoleService processRoleService;

    private final AuthorityService authorityService;

    @Override
    public void onAfterLoad(AfterLoadEvent<AbstractActor> event) {
        log.trace("User loaded: {}", event.getSource().get("id"));
        log.trace("Resolving process roles of user with username: {}", event.getSource().get("id"));
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<AbstractActor> event) {
        AbstractActor actor = event.getSource();
        actor.getProcessRoleIds().forEach(processRoleId -> {
            ProcessRole role = processRoleService.findById(processRoleId);
            if (role != null) {
                actor.addProcessRole(role);
            }
        });
        actor.getAuthorityIds().forEach(authorityId -> {
            Authority authority = authorityService.getOne(authorityId);
            if (authority != null) {
                actor.addAuthority(authority);
            }
        });
    }
}
