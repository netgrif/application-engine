package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.objects.workflow.domain.Task
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ServiceMethodAuthorizationTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Test
    void testAllServiceButtonsPostSetActions() {
        testHelper.truncateDbs()

        def netOptional = importHelper.createNet("service_methods_test.xml")
        assertTrue(netOptional.isPresent())
        def net = netOptional.get()

        Case useCase = importHelper.createCase("Service Test Case", net)
        assertNotNull(useCase)

        Task task = taskService.findOne(useCase.tasks.first().task)
        assertNotNull(task)

        // Trigger set on each button field
        net.dataSet.values().findAll { it.type.name().equalsIgnoreCase("BUTTON") }.each { buttonField ->
            def outcome = dataService.setData(task.stringId, ImportHelper.populateDataset([
                    (buttonField.stringId): [
                            "type": "button"
                    ]
            ]))
            assertNotNull(outcome)
        }
    }

    @Test
    void testAllAuthorityServiceButtonsPostSetActions() {
        testHelper.truncateDbs()

        def netOptional = importHelper.createNet("authority_service_methods_test.xml")
        assertTrue(netOptional.isPresent())
        def net = netOptional.get()

        Case useCase = importHelper.createCase("Authority Service Test Case", net)
        assertNotNull(useCase)

        Task task = taskService.findOne(useCase.tasks.first().task)
        assertNotNull(task)

        // Trigger set on each button field
        net.dataSet.values().findAll { it.type.name().equalsIgnoreCase("BUTTON") }.each { buttonField ->
            def outcome = dataService.setData(task.stringId, ImportHelper.populateDataset([
                    (buttonField.stringId): [
                            "type": "button"
                    ]
            ]))
            assertNotNull(outcome)
        }
    }

    @Test
    void testAllAuthorityApiButtonsPostSetActions() {
        executeAllButtonsOfNet("authority_api_methods_test.xml", "Authority API Test Case")
    }

    private void executeAllButtonsOfNet(String netFileName, String caseTitle) {
        testHelper.truncateDbs()

        def netOptional = importHelper.createNet(netFileName)
        assertTrue(netOptional.isPresent())
        def net = netOptional.get()

        Case useCase = importHelper.createCase(caseTitle, net)
        assertNotNull(useCase)

        Task task = taskService.findOne(useCase.tasks.first().task)
        assertNotNull(task)

        // Trigger set on each button field
        net.dataSet.values().findAll { it.type.name().equalsIgnoreCase("BUTTON") }.each { buttonField ->
            def outcome = dataService.setData(task.stringId, ImportHelper.populateDataset([
                    (buttonField.stringId): [
                            "type": "button"
                    ]
            ]))
            assertNotNull(outcome)
        }
    }
}
