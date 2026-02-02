package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.auth.repository.AnonymousUserRefRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

public class AnonymousUserRefServiceImpl implements AnonymousUserRefService {

    @Autowired
    private AnonymousUserRefRepository repository;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Override
    public AnonymousUserRef getOrCreateRef(String realmId) {
        return repository.findByRealmId(realmId)
                .orElseGet(() -> repository.save(new AnonymousUserRef(realmId, Set.of(authorityService.getOrCreate(Authority.anonymous)), Set.of(processRoleService.getAnonymousRole()))));
    }

    @Override
    public Optional<AnonymousUserRef> getRef(String realmId) {
        return repository.findByRealmId(realmId);
    }

    @Override
    public void deleteRef(String realmId) {
        repository.findByRealmId(realmId).ifPresent(repository::delete);
    }

}
