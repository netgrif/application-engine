package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension.class)
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

    private static final FUNCTION_OVERLOADING_IDENTIFIER = "function_overloading"

    @Value("classpath:petriNets/function_res.xml")
    private Resource functionResNetResource

    @Value("classpath:petriNets/function_res_v2.xml")
    private Resource functionResNetResourceV2

    @Value("classpath:petriNets/function_test.xml")
    private Resource functionTestNetResource

    @Value("classpath:petriNets/function_test_v2.xml")
    private Resource functionTestNetResourceV2

    @Value("classpath:petriNets/function_overloading.xml")
    private Resource functionOverloadingNetResource

    @Value("classpath:petriNets/function_overloading_v2.xml")
    private Resource functionOverloadingNetResourceV2

    @Value("classpath:petriNets/function_overloading_fail.xml")
    private Resource functionOverloadingFailNetResource

    @Value("classpath:petriNets/function_overloading_fail_v2.xml")
    private Resource functionOverloadingFailNetResourceV2

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    @Disabled("MissingMethod No signature of method")
    void testNamespaceFunction() {
        assert userService.findByEmail("test@test.com", true) == null

        def functionResNet = petriNetService.importPetriNet(functionResNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()

        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["createUser": ["value": "true", "type": "boolean"]]))

        User user = userService.findByEmail("test@test.com", true)
        assert user

        userService.deleteUser(user)
        petriNetService.deletePetriNet(functionResNet.stringId, userService.getLoggedOrSystem().transformToLoggedUser())
        petriNetService.deletePetriNet(functionTestNet.stringId, userService.getLoggedOrSystem().transformToLoggedUser())
    }

    @Test
    void testProcessFunctions() {
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        assert functionTestNet

        Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["enum": ["value": "ano", "type": "enumeration"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getDataField("number").isRequired(aCase.tasks.first().transition)
        def fieldBehavior = aCase.getDataField("number").behavior
        assert fieldBehavior.containsKey(aCase.tasks.first().transition) && fieldBehavior.get(aCase.tasks.first().transition).contains(FieldBehavior.EDITABLE)

        petriNetService.deletePetriNet(functionTestNet.stringId, userService.getLoggedOrSystem().transformToLoggedUser())
    }

    @Test
    void testNamespaceFunctionException() {
        assertThrows(MissingMethodException.class, () -> {
            def nets = petriNetService.getByIdentifier(FUNCTION_RES_IDENTIFIER)
            if (nets) {
                nets.each {
                    petriNetService.deletePetriNet(it.getStringId(), userService.getLoggedOrSystem().transformToLoggedUser())
                }
            }

            def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
            assert functionTestNet

            Case aCase = workflowService.createCase(functionTestNet.get().stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser())
            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        })
    }

    @Test
    void testNewNetVersionMissingMethodException() {
        assertThrows(NullPointerException.class, () -> {
            def nets = petriNetService.getByIdentifier(FUNCTION_TEST_IDENTIFIER)
            if (nets) {
                nets.each {
                    petriNetService.deletePetriNet(it.getStringId(), userService.getLoggedOrSystem().transformToLoggedUser())
                }
            }

            def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
            assert functionTestNet

            Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["text": ["value": "20", "type": "text"]]))

            functionTestNet = petriNetService.importPetriNet(functionTestNetResourceV2.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
            assert functionTestNet

            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["text": ["value": "20", "type": "text"]]))
        })
    }

    @Test
    void testProcessFunctionException() {
        assertThrows(MissingMethodException.class, () -> {
            def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
            assert functionTestNet

            Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number3": ["value": "20", "type": "number"]]))
        })
    }

    @Test
    void testNewVersionOfNamespaceFunction() {
        def functionResNet = petriNetService.importPetriNet(functionResNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        def functionTestNet = petriNetService.importPetriNet(functionTestNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()

        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 + 20

        functionResNet = petriNetService.importPetriNet(functionResNetResourceV2.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        assert functionResNet

        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 * 20
    }

    @Test
    void testNamespaceMethodOverloadingFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(functionOverloadingFailNetResource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        })
    }

    @Test
    void testNamespaceUseCaseUpdate() {
        def functionResV2Net = petriNetService.importPetriNet(functionResNetResourceV2.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()
        def functionTestV2Net = petriNetService.importPetriNet(functionTestNetResourceV2.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()

        Case aCase = workflowService.createCase(functionTestV2Net.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["updateOtherField": ["value": "true", "type": "boolean"]]))

        aCase = workflowService.findOne(aCase.stringId)
        assert aCase.getFieldValue("toBeUpdated") as Double == 1.0
        assert aCase.getFieldValue("toBeUpdatedInternally") as Double == 2.0
    }

    @Test
    void testNamespaceMethodOverloading() {
        testMethodOverloading(functionOverloadingNetResource)
    }

    @Test
    void testProcessMethodOverloadingFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(functionOverloadingFailNetResourceV2.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser())
        })
    }

    @Test
    void testProcessMethodOverloading() {
        testMethodOverloading(functionOverloadingNetResourceV2)
    }

    private void testMethodOverloading(Resource resource) {
        def petriNet = petriNetService.importPetriNet(resource.inputStream, VersionType.MAJOR, userService.getLoggedOrSystem().transformToLoggedUser()).getNet()

        assert petriNet

        Case aCase = workflowService.createCase(petriNet.stringId, "Test", "", userService.getLoggedOrSystem().transformToLoggedUser()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 * 20
        assert aCase.getFieldValue("text") == "20.0 20.0"
    }
}
