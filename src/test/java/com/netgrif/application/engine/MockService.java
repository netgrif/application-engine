package com.netgrif.application.engine;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.LoggedUser;
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

    public LoggedUser mockLoggedUser() {
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        return new LoggedUser(new ObjectId().toString(), configuration.getEmail(), configuration.getPassword(), Collections.singleton(authorityUser));
    }
}
