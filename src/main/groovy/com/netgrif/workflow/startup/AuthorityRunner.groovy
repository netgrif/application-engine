package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.service.interfaces.IAuthorityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AuthorityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IAuthorityService service

    @Override
    void run(String... strings) throws Exception {
        service.getOrCreate(Authority.user)
        service.getOrCreate(Authority.admin)
        service.getOrCreate(Authority.system)
    }
}