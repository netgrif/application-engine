package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

@Slf4j
public class AnonymousUserRefServiceImpl implements AnonymousUserRefService {

    @Autowired
    private RealmService realmService;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Override
    public AnonymousUserRef getOrCreateRef(String realmId) {
        return new AnonymousUserRef(realmId, Set.of(authorityService.getOrCreate(Authority.anonymous)), Set.of(processRoleService.getAnonymousRole()));
    }

    @Override
    public Optional<AnonymousUserRef> getRef(String realmId) {
        Optional<Realm> realmOptional = realmService.getRealmById(realmId);
        if (realmOptional.isEmpty()) {
            throw new IllegalArgumentException("Realm with id " + realmId + " not found");
        }
        Realm realm = realmOptional.get();
        if (!realm.isPublicAccess()) {
            log.warn("Public access is disabled for realm {}.", realmId);
            return Optional.empty();
        }
        return Optional.of(new AnonymousUserRef(realmId, Set.of(authorityService.getOrCreate(Authority.anonymous)), Set.of(processRoleService.getAnonymousRole())));
    }

}
