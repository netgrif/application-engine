package com.netgrif.application.engine;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class MockService {

    @Autowired
    private SuperAdminConfiguration configuration;

    public LoggedIdentity mockLoggedIdentity() {
        return LoggedIdentity.with()
                .identityId(new ObjectId().toString())
                .username(configuration.getEmail())
                .password(configuration.getPassword())
                .activeActorId(new ObjectId().toString())
                .build();
    }
}
