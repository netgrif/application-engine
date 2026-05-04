package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.AbstractActor;
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
        log.trace("User loaded: {}", event.getSource());
        log.trace("Resolving process roles of user with id: {}", event.getSource());
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<AbstractActor> event) {
        AbstractActor actor = event.getSource();
        actor.getProcessRoleIds().forEach(processRoleId -> actor.getProcessRoles().add(processRoleService.findById(processRoleId)));
        actor.getAuthorityIds().forEach(authorityId -> actor.getAuthoritySet().add(authorityService.getOne(authorityId)));
    }
}
