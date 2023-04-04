package com.netgrif.application.engine.importer

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.importer.service.AllDataConfiguration
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.Transition
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
@CompileStatic
class AllDataTransitionTest {

    @Autowired
    private EngineTest testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Value("classpath:petriNets/NAE-1858_allDataTask.xml")
    private Resource resourceFile

    @Autowired
    private AllDataConfiguration configuration

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testAllData() throws MissingPetriNetMetaDataException, IOException {
        ImportPetriNetEventOutcome outcome = petriNetService.importPetriNet(resourceFile.inputStream, VersionType.MAJOR, superCreator.getLoggedSuper());
        assert outcome.getNet() != null;
        PetriNet net = outcome.getNet()

        assert net.transitions.size() == 3
        Transition allData = net.getTransition(configuration.allData.id)
        assert allData
        assert allData.dataSet.size() == net.dataSet.size()
    }
}
