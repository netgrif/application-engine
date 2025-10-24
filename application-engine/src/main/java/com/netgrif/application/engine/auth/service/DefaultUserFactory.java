package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.User;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserFactory implements ActorTransformer.UserFactory {

    @Override
    public AbstractUser create() {
        User user = new User();
        return user;
    }
}

