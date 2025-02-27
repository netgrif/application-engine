package com.netgrif.application.engine;

import com.netgrif.adapter.auth.domain.LoggedUserImpl;
import com.netgrif.core.auth.domain.Authority;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.auth.service.AuthorityService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Profile("test")
public class MockService {

    @Autowired
    private AuthorityService authorityService;

    public LoggedUser mockLoggedUser() {
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        return new LoggedUserImpl(new ObjectId().toString(), "super@netgrif.com", "password", Collections.singleton(authorityUser), Collections.emptySet());
    }
}
