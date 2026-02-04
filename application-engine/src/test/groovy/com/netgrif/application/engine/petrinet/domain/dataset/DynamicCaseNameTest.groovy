package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
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
class DynamicCaseNameTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/dynamic_case_name_test.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        Case useCase = workflowService.createCase(CreateCaseParams.with()
                .process(optNet.getNet())
                .color("")
                .author(superCreator.loggedSuper)
                .locale(Locale.forLanguageTag("sk-SK"))
                .build()).getCase()
        assert useCase.title == "SK text value 6"

        Case useCase2 = workflowService.createCase(CreateCaseParams.with()
                .process(optNet.getNet())
                .color("")
                .author(superCreator.loggedSuper)
                .locale(Locale.ENGLISH)
                .build()).getCase()
        assert useCase2.title == "EN text value 6"
    }
}
