package com.netgrif.workflow.petrinet.domain

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class FunctionsTest {

    @Autowired
    private IUserService userService

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private TestHelper testHelper

    private static final FUNCTION_RES_IDENTIFIER = "function_res"

    private static final FUNCTION_TEST_IDENTIFIER = "function_test"

    @Value("classpath:petriNets/function_res.xml")
    private Resource functionResNetResource

    @Value("classpath:petriNets/function_res_v2.xml")
    private Resource functionResNetResourceV2

    @Value("classpath:petriNets/function_test.xml")
    private Resource functionTestNetResource

    @Value("classpath:petriNets/function_test_v2.xml")
    private Resource functionTestNetResourceV2

    @Test
    void testNamespaceFunction() {
        assert userService.findByEmail("test@test.com", true) == null

        def functionResNet = petriNetService.importPetriNet(functionResNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())

        assert functionResNet.isPresent()
        assert functionTestNet.isPresent()

        Case aCase = workflowService.createCase(functionTestNet.get().stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["createUser": ["value": "true", "type": "boolean"]]))

        User user = userService.findByEmail("test@test.com", true)
        assert user

        userService.deleteUser(user)
        petriNetService.deletePetriNet(functionResNet.get().stringId, userService.getLoggedOrSystem().transformToLoggedUser())
        petriNetService.deletePetriNet(functionTestNet.get().stringId, userService.getLoggedOrSystem().transformToLoggedUser())
    }

    @Test
    void testProcessFunctions() {
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        assert functionTestNet.isPresent()

        Case aCase = workflowService.createCase(functionTestNet.get().stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["enum": ["value": "ano", "type": "enumeration"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getDataField("number").isRequired(aCase.tasks.first().transition)
        def fieldBehavior = aCase.getDataField("number").behavior
        assert fieldBehavior.containsKey(aCase.tasks.first().transition) && fieldBehavior.get(aCase.tasks.first().transition).contains(FieldBehavior.EDITABLE)

        petriNetService.deletePetriNet(functionTestNet.get().stringId, userService.getLoggedOrSystem().transformToLoggedUser())
    }

    @Test(expected = Exception.class)
    void testNamespaceFunctionException() {
        def nets = petriNetService.getByIdentifier(FUNCTION_RES_IDENTIFIER)
        if (nets) {
            nets.each {
                petriNetService.deletePetriNet(it.getStringId(), userService.getLoggedOrSystem().transformToLoggedUser())
            }
        }

        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        assert functionTestNet.isPresent()

        Case aCase = workflowService.createCase(functionTestNet.get().stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
    }

    @Test(expected = Exception.class)
    void testProcessFunctionException() {
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        assert functionTestNet.isPresent()

        Case aCase = workflowService.createCase(functionTestNet.get().stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number3": ["value": "20", "type": "number"]]))
    }

    @Test
    void testNewVersionOfNamespaceFunction() {
        def functionResNet = petriNetService.importPetriNet(functionResNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())

        assert functionResNet.isPresent()
        assert functionTestNet.isPresent()

        Case aCase = workflowService.createCase(functionTestNet.get().stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 + 20

        functionResNet = petriNetService.importPetriNet(functionResNetResourceV2.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        assert functionResNet.isPresent()

        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 * 20
    }

    @After
    void after() {
        def resNets = petriNetService.getByIdentifier(FUNCTION_RES_IDENTIFIER)
        resNets += petriNetService.getByIdentifier(FUNCTION_TEST_IDENTIFIER)
        if (!resNets) {
            resNets.each {
                petriNetService.deletePetriNet(it.getStringId(), userService.getLoggedOrSystem().transformToLoggedUser())
            }
        }
    }
}
