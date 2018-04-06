package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.domain.LoggedUser
import com.netgrif.workflow.petrinet.service.PetriNetService
import com.netgrif.workflow.petrinet.web.requestbodies.UploadedFileMeta
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component

@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner{

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private PetriNetService petriNetService

    @Override
    void run(String... strings) throws Exception {
        superCreator.setAllToSuperUser()
    }
}
