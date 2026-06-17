package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;

import java.util.Optional;

public interface AnonymousUserRefService {

    AnonymousUserRef getOrCreateRef(String realmId);

    Optional<AnonymousUserRef> getRef(String realmId);

    void deleteRef(String realmId);
}
