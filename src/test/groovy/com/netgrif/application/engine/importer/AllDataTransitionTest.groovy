package com.netgrif.application.engine.importer

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.AllDataConfiguration
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.workflow.domain.Transition
import com.netgrif.application.engine.workflow.domain.VersionType
import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportProcessEventOutcome
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
    private TestHelper testHelper

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
    void testAllData() throws MissingProcessMetaDataException, IOException {
        ImportProcessEventOutcome outcome = petriNetService.importPetriNet(resourceFile.inputStream, VersionType.MAJOR, superCreator.getLoggedSuper());
        assert outcome.getNet() != null;
        Process net = outcome.getNet()

        assert net.transitions.size() == 3
        Transition allData = net.getTransition(configuration.allData.id)
        assert allData
        assert allData.dataSet.size() == net.dataSet.size()
    }
}
