package com.netgrif.application.engine.startup

import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UriRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IUriService uriService


    @Override
    void run(String... args) throws Exception {
        uriService.createDefault()
    }
}
