package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@ActiveProfiles(["test"])
@RunWith(SpringRunner.class)
class DynamicDefaultValueTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testDynamicEnum() {
        Optional<PetriNet> optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_init.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Case useCase = importHelper.createCase("test", optNet.get())

        assert useCase.dataSet["text"].value == superCreator.superUser.name
        assert useCase.dataSet["number"].value as Integer ==  superCreator.superUser.name.length()
        assert useCase.dataSet["date"].value != null
        assert useCase.dataSet["dateTime"].value != null
        assert (useCase.dataSet["user"].value as User) != null
    }
}
