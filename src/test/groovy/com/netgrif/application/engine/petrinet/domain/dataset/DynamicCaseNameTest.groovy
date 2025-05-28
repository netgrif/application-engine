package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.params.ImportProcessParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
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
    private SuperCreator superCreator

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        TestHelper.login(superCreator.superIdentity)
    }

    @Test
    void testInitValues() {
        ImportPetriNetEventOutcome optNet = petriNetService.importProcess(new ImportProcessParams(new FileInputStream("src/test/resources/petriNets/dynamic_case_name_test.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper().getActiveActorId()))
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .process(optNet.getProcess())
                .authorId(superCreator.loggedSuper.activeActorId)
                .locale(Locale.forLanguageTag("sk-SK"))
                .build()
        Case useCase = workflowService.createCase(createCaseParams).getCase()
        assert useCase.title == "SK text value 6"

        createCaseParams = CreateCaseParams.with()
                .process(optNet.getProcess())
                .authorId(superCreator.loggedSuper.activeActorId)
                .locale(Locale.ENGLISH)
                .build()
        Case useCase2 = workflowService.createCase(createCaseParams).getCase()
        assert useCase2.title == "EN text value 6"
    }
}
