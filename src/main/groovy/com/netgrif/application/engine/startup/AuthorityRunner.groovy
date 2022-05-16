package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.domain.AuthorizingObject
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Slf4j
@Component
class AuthorityRunner extends AbstractOrderedCommandLineRunner {

    @Value('${nae.authority.additional:}')
    private String[] additionalAuthorities

    @Autowired
    private IAuthorityService service

    @Override
    void run(String... strings) throws Exception {
        AuthorizingObject.values().toList().forEach(authority -> service.getOrCreate(authority.name()))
        additionalAuthorities.toList().forEach(authority -> service.getOrCreate(authority))
        log.info("Authorities created.")
    }
}