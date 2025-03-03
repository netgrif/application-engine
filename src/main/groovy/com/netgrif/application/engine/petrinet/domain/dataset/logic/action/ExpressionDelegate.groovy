package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.authentication.service.interfaces.IUserService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

@Slf4j
class ExpressionDelegate {

    // TODO: release/8.0.0 services and methods?
    @Autowired
    IUserService userService
}
