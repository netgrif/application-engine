package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class AuthorityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IAuthorityService service

    @Override
    void run(String... strings) throws Exception {
        service.getOrCreate(Authority.user)
        service.getOrCreate(Authority.admin)
        service.getOrCreate(Authority.systemAdmin)
        service.getOrCreate(Authority.anonymous)
    }
}