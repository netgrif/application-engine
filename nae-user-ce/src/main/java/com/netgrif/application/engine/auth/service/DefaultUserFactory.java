package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUser;
import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultUserFactory implements ActorTransformer.UserFactory {

    private final AnonymousUserRefService anonymousUserRefService;

    @Override
    public AbstractUser create() {
        return new User();
    }

    @Override
    public AbstractUser create(LoggedUser loggedUser) {
        if (!loggedUser.isAnonymous() || loggedUser.getRealmId() == null) {
            return create();
        }
        Optional<AnonymousUserRef> refOpt = anonymousUserRefService.getRef(loggedUser.getRealmId());
        if (refOpt.isEmpty()) {
            throw new IllegalStateException("Anonymous user reference not found for realm [%s]".formatted(loggedUser.getRealmId()));
        }
        return new AnonymousUser(refOpt.get());
    }
}

