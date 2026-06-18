package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.params.DeletePetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.events.DataEventType
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ActionRefTest {

    public static final String NET_FILE = "src/test/resources/actionref_test.xml"
    public static final String NET_IDENTIFIER = "actionref_test.xml"

    @Autowired
    private PetriNetRepository netRepository

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private IPetriNetService petriNetService;

    @BeforeEach
    void before() {
        netRepository.findByIdentifier(NET_IDENTIFIER, Pageable.unpaged()).content.each {
            petriNetService.forceDeletePetriNet(new DeletePetriNetParams(it.stringId, superCreator.getLoggedSuper()))
        }
    }

    @Test
    void testEventImport() {

        PetriNet net = petriNetService.importPetriNet(new ImportPetriNetParams(new FileInputStream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())).getNet()

        assert net.dataSet.get("text_1").events.keySet().containsAll([DataEventType.GET, DataEventType.SET])
        assert net.transitions.get("task").dataSet.get("text_1").events.keySet().containsAll([DataEventType.GET, DataEventType.SET])
    }
}
