package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoggedUserConfiguration {

    private final ActorTransformer.LoggedUserFactory loggedUserFactory;
    private final ActorTransformer.UserFactory userFactory;

    @PostConstruct
    public void initializeLoggedUserFactory() {
        ActorTransformer.setLoggedUserFactory(loggedUserFactory);
        ActorTransformer.setUserFactory(userFactory);
    }
}
