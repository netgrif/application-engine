package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.service.interfaces.IUserService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class AnonymousIdentityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IUserService userService

    @Autowired
    private IIdentityService identityService

    @Override
    void run(String... args) throws Exception {
        Set<String> keywords = Set.of(AnonymIdentityConstants.defaultUsername())
        userService.registerForbiddenKeywords(keywords)
        identityService.registerForbiddenKeywords(keywords)
    }
}
