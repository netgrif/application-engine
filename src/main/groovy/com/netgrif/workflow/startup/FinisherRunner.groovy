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
        LoggedUser user = new LoggedUser(1,"Super","password",new ArrayList<GrantedAuthority>())

        petriNetService.importPetriNet(new File("src/main/resources/petriNets/insurance_portal_demo.xml"),
                new UploadedFileMeta("Insurance prve","INS","Insurance","MAJOR"),user)
        petriNetService.importPetriNet(new File("src/main/resources/petriNets/insurance_portal_demo.xml"),
                new UploadedFileMeta("Insurance druhe","INS","Insurance","Patch"),user)
        petriNetService.importPetriNet(new File("src/main/resources/petriNets/insurance_portal_demo.xml"),
                new UploadedFileMeta("Insurance third","INS","Insurance","minor"),user)
        petriNetService.importPetriNet(new File("src/main/resources/petriNets/insurance_role_test.xml"),
                new UploadedFileMeta("Insurance foooo","INS","Insurance","patch"),user)

        petriNetService.importPetriNet(new File("src/main/resources/petriNets/FM_v0_2.xml")
                , new UploadedFileMeta("FMko prvé", "FM", "FM", "Major"),user)
        petriNetService.importPetriNet(new File("src/main/resources/petriNets/FM_v0_2.xml")
                , new UploadedFileMeta("FMko druhé", "FM", "FM", "minor"),user)
        petriNetService.importPetriNet(new File("src/main/resources/petriNets/FM_v0_2.xml")
                , new UploadedFileMeta("FMko tretucke", "FM", "FM", "patch"),user)

        petriNetService.getReferencesByVersion("^",user, Locale.US)


        superCreator.setAllToSuperUser()
    }
}
