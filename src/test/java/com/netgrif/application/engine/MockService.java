package com.netgrif.application.engine;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@Profile("test")
public class MockService {

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IProcessRoleService processRoleService;

    public LoggedUser mockLoggedUser() {
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        LoggedUser loggedUser = new LoggedUser(new ObjectId().toString(), "super@netgrif.com", "password", Collections.singleton(authorityUser));
        loggedUser.setProcessRoles(Set.of(processRoleService.defaultRole().getStringId()));
        return loggedUser;
    }
}
