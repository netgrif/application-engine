package com.netgrif.application.engine;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorityProperties;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Profile("test")
public class MockService {

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private AuthorityProperties authorityProperties;

    public LoggedUser mockLoggedUser() {
        List<Authority> authorityUser = authorityService.getOrCreate(authorityProperties.getDefaultUserAuthorities());
        return new LoggedUser(new ObjectId().toString(), "super@netgrif.com", "password", authorityUser);
    }
}
