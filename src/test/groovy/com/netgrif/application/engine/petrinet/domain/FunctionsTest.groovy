package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
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

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.EDITABLE
import static org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class FunctionsTest {

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IIdentityService identityService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private TestHelper testHelper

    private static final String FUNCTION_RES_IDENTIFIER = "function_res"

    private static final String FUNCTION_TEST_IDENTIFIER = "function_test"

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
        TestHelper.login(superCreator.superIdentity)
    }

    @Test
    void testNamespaceFunction() {
        assert identityService.findByUsername("test@test.com").isEmpty()

        def functionResNet = petriNetService.importProcess(functionResNetResource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        def functionTestNet = petriNetService.importProcess(functionTestNetResource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()

        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "",
                superCreator.getLoggedSuper().activeActorId).getCase()
        dataService.setData(aCase.getTaskStringId("t1"), new DataSet(["createUser": new BooleanField(rawValue: true)] as Map<String, Field<?>>),
                superCreator.getLoggedSuper().activeActorId)

        Optional<Identity> identityOpt = identityService.findByUsername("test@test.com")
        assert identityOpt.isPresent()

        // todo: release/8.0.0 removal
//        userService.deleteUser(user)
        petriNetService.deleteProcess(functionResNet.stringId)
        petriNetService.deleteProcess(functionTestNet.stringId)
    }

    @Test
    void testProcessFunctions() {
        def functionResNet = petriNetService.importProcess(functionResNetResource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        def functionTestNet = petriNetService.importProcess(functionTestNetResource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "", superCreator.getLoggedSuper().activeActorId).getCase()
        dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["enum": new EnumerationField(rawValue: new I18nString("ano"))] as Map<String, Field<?>>)),
                superCreator.getLoggedSuper().activeActorId)
        aCase = workflowService.findOne(aCase.getStringId())

        NumberField field = aCase.getDataSet().get("number") as NumberField
        assert field.getBehaviors().get("t1").required
        def fieldBehavior = field.behaviors
        assert fieldBehavior.get("t1").behavior == EDITABLE

        petriNetService.deleteProcess(functionTestNet.stringId)
    }

    @Test
    void testNamespaceFunctionException() {
        assertThrows(NullPointerException.class, () -> {
            def functionResNet = petriNetService.importProcess(functionResNetResource.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId).getProcess()
            assert functionResNet

            def nets = petriNetService.getByIdentifier(FUNCTION_RES_IDENTIFIER)
            if (nets) {
                nets.each {
                    petriNetService.deleteProcess(it.getStringId())
                }
            }

            def functionTestNet = petriNetService.importProcess(functionTestNetResource.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId).getProcess()
            assert functionTestNet

            Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "",
                    superCreator.getLoggedSuper().activeActorId).getCase()
            dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["number": new NumberField(rawValue: 20d)] as Map<String, Field<?>>)),
                    superCreator.getLoggedSuper().activeActorId)
        })
    }

    @Test
    void testNewNetVersionMissingMethodException() {
        assertThrows(NullPointerException.class, () -> {
            def nets = petriNetService.getByIdentifier(FUNCTION_TEST_IDENTIFIER)
            if (nets) {
                nets.each {
                    petriNetService.deleteProcess(it.getStringId())
                }
            }

            def functionTestNet = petriNetService.importProcess(functionTestNetResource.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId).getProcess()
            assert functionTestNet

            Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "",
                    superCreator.getLoggedSuper().activeActorId).getCase()
            dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["text": new TextField(rawValue: "20")] as Map<String, Field<?>>)),
                    superCreator.getLoggedSuper().activeActorId)

            functionTestNet = petriNetService.importProcess(functionTestNetResourceV2.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId).getProcess()
            assert functionTestNet

            dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["text": new TextField(rawValue: "20")] as Map<String, Field<?>>)),
                    superCreator.getLoggedSuper().activeActorId)
        })
    }

    @Test
    void testProcessFunctionException() {
        assertThrows(MissingMethodException.class, () -> {
            def functionTestNet = petriNetService.importProcess(functionTestNetResource.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId).getProcess()
            assert functionTestNet

            Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "",
                    superCreator.getLoggedSuper().activeActorId).getCase()
            dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["number3": new NumberField(rawValue: 20d)] as Map<String, Field<?>>)),
                    superCreator.getLoggedSuper().activeActorId)
        })
    }

    @Test
    void testNewVersionOfNamespaceFunction() {
        def functionResNet = petriNetService.importProcess(functionResNetResource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        def functionTestNet = petriNetService.importProcess(functionTestNetResource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()

        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(functionTestNet.stringId, "Test", "",
                superCreator.getLoggedSuper().activeActorId).getCase()
        dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["number": new NumberField(rawValue: 20d)] as Map<String, Field<?>>)),
                superCreator.getLoggedSuper().activeActorId)
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getDataSet().get("number2").rawValue == 20 + 20

        functionResNet = petriNetService.importProcess(functionResNetResourceV2.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        assert functionResNet

        dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["number": new NumberField(rawValue: 20d)] as Map<String, Field<?>>)),
                superCreator.getLoggedSuper().activeActorId)
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getDataSet().get("number2").rawValue == 20 * 20
    }

    @Test
    void testNamespaceMethodOverloadingFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importProcess(functionOverloadingFailNetResource.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId)
        })
    }

    @Test
    void testNamespaceUseCaseUpdate() {
        def functionResV2Net = petriNetService.importProcess(functionResNetResourceV2.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        def functionTestV2Net = petriNetService.importProcess(functionTestNetResourceV2.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()

        Case aCase = workflowService.createCase(functionTestV2Net.stringId, "Test", "", superCreator.getLoggedSuper().activeActorId).getCase()
        dataService.setData(aCase.getTaskStringId("t0"), new DataSet((["updateOtherField": new BooleanField(rawValue: true)] as Map<String, Field<?>>)),
                superCreator.getLoggedSuper().activeActorId)

        aCase = workflowService.findOne(aCase.stringId)
        assert aCase.getDataSet().get("toBeUpdated").rawValue == 1.0
        assert aCase.getDataSet().get("toBeUpdatedInternally").rawValue == 2.0
    }

    @Test
    void testNamespaceMethodOverloading() {
        testMethodOverloading(functionOverloadingNetResource)
    }

    @Test
    void testProcessMethodOverloadingFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importProcess(functionOverloadingFailNetResourceV2.inputStream, VersionType.MAJOR,
                    superCreator.getLoggedSuper().activeActorId)
        })
    }

    @Test
    void testProcessMethodOverloading() {
        testMethodOverloading(functionOverloadingNetResourceV2)
    }

    private void testMethodOverloading(Resource resource) {
        def petriNet = petriNetService.importProcess(resource.inputStream, VersionType.MAJOR,
                superCreator.getLoggedSuper().activeActorId).getProcess()
        assert petriNet

        Case aCase = workflowService.createCase(petriNet.stringId, "Test", "", superCreator.getLoggedSuper().activeActorId).getCase()
        dataService.setData(aCase.getTaskStringId("t1"), new DataSet((["number": new NumberField(rawValue: 20d)] as Map<String, Field<?>>)),
                superCreator.getLoggedSuper().activeActorId)
        aCase = workflowService.findOne(aCase.getStringId())
        NumberField numberField2 = aCase.dataSet.get("number2") as NumberField
        TextField textField = aCase.dataSet.get("text") as TextField

        assert numberField2.rawValue == 20 * 20
        assert textField.rawValue == "20.0 20.0"
    }
}
