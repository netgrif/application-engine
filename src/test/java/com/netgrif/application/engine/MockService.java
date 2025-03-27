package com.netgrif.application.engine;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Profile("test")
public class MockService {

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private SuperAdminConfiguration configuration;

    public Identity mockLoggedUser() {
        SessionRole sessionRoleUser = authorityService.getOrCreate(SessionRole.user);
        return new Identity(new ObjectId().toString(), configuration.getEmail(), configuration.getPassword(), Collections.singleton(sessionRoleUser));
    }
}
