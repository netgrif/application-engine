package com.netgrif.application.engine;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.objects.auth.domain.enums.UserType;
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
    private AuthorityService authorityService;

    public LoggedUser mockLoggedUser() {
        Authority authorityUser = authorityService.getOrCreate(Authority.user);
        LoggedUser loggedUser = new LoggedUserImpl(new ObjectId(), null, "testUsername", "testFirstName", "testMiddleName", "testLastName", "test@email.com", "", null, null, null, null, UserType.INTERNAL);
        loggedUser.setAuthoritySet(Set.of(authorityUser));
        return loggedUser;

    }
}
