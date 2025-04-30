package com.netgrif.application.engine.adapter.spring.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

//TODO: presun totok!

@Service
public class AnonymousUserRefServiceImpl implements AnonymousUserRefService {

    @Autowired
    private AnonymousUserRefRepository repository;

    @Override
    public AnonymousUserRef getOrCreateRef(String realmId) {
        return repository.findByRealmId(realmId)
                .orElseGet(() -> repository.save(new AnonymousUserRef(realmId)));
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
