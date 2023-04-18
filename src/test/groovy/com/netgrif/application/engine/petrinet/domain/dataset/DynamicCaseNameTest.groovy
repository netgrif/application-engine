package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
@CompileStatic
class DynamicCaseNameTest extends EngineTest {

    @BeforeEach
    void before() {
        truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_case_name_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Case useCase = workflowService.createCase(optNet.getNet().stringId, null, "", superCreator.loggedSuper, Locale.forLanguageTag("sk-SK")).getCase()
        assert useCase.title == "SK text value 6"

        Case useCase2 = workflowService.createCase(optNet.getNet().stringId, null, "", superCreator.loggedSuper, Locale.ENGLISH).getCase()
        assert useCase2.title == "EN text value 6"
    }
}
