package com.netgrif.workflow;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Profile("test")
public class MockService {

    @Autowired
    private IAuthorityService authorityService;

    public LoggedUser mockLoggedUser() {
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        return new LoggedUser(1L, "super@netgrif.com", "password", Collections.singleton(authorityUser));
    }
}
