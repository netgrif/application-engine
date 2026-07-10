package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.AbstractActor;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserMongoEventListener implements AfterConvertCallback<AbstractActor> {

    private final ProcessRoleService processRoleService;

    private final AuthorityService authorityService;

    public UserMongoEventListener(@Lazy ProcessRoleService processRoleService,
                                  @Lazy AuthorityService authorityService) {
        this.processRoleService = processRoleService;
        this.authorityService = authorityService;
    }

    @Override
    @NonNull
    public AbstractActor onAfterConvert(AbstractActor entity, @NonNull Document document, @NonNull String collection) {
        entity.getProcessRoleIds().forEach(processRoleId -> {
            ProcessRole role = processRoleService.findById(processRoleId);
            if (role != null) {
                entity.getProcessRoles().add(role);
            }
        });
        entity.getAuthorityIds().forEach(authorityId -> {
            Authority authority = authorityService.getOne(authorityId);
            if (authority != null) {
                entity.getAuthoritySet().add(authority);
            }
        });
        return entity;
    }
}
