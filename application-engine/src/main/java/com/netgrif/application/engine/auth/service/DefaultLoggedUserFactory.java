package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoggedUserFactory implements ActorTransformer.LoggedUserFactory {

    @Override
    public LoggedUser create() {
        LoggedUser loggedUser = new LoggedUserImpl();
        return loggedUser;
    }
}
