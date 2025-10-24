package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.service.DefaultLoggedUserFactory;
import com.netgrif.application.engine.auth.service.DefaultUserFactory;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggedUserConfiguration {

    @PostConstruct
    public void initializeLoggedUserFactory() {
        ActorTransformer.setLoggedUserFactory(new DefaultLoggedUserFactory());
        ActorTransformer.setUserFactory(new DefaultUserFactory());
    }
}
