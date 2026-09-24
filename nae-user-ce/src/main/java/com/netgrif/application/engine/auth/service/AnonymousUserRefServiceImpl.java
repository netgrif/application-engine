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
        Realm realm = resolveRealm(realmId);
        return new AnonymousUserRef(realm.getName(), Set.of(authorityService.getOrCreate(Authority.anonymous)), Set.of(processRoleService.getAnonymousRole()));
    }

    @Override
    public Optional<AnonymousUserRef> getRef(String realmId) {
        Realm realm = resolveRealm(realmId);
        if (!realm.isPublicAccess()) {
            log.warn("Public access is disabled for realm {}.", realm.getName());
            return Optional.empty();
        }
        return Optional.of(new AnonymousUserRef(realm.getName(), Set.of(authorityService.getOrCreate(Authority.anonymous)), Set.of(processRoleService.getAnonymousRole())));
    }

    private Realm resolveRealm(String realmId) {
        if (realmId == null || realmId.isBlank() || "null".equals(realmId)) {
            return realmService.getDefaultRealm()
                    .orElseThrow(() -> new IllegalArgumentException("Default realm was not found"));
        }
        return realmService.getRealmById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
    }

}
