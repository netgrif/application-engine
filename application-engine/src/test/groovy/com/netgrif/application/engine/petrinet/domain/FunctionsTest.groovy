package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.params.DeletePetriNetParams
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.workflow.params.CreateCaseParams
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
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class FunctionsTest {

    @Autowired
    private UserService userService

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
        assert userService.findUserByUsername("test@test.com", null) == null

        def functionResNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionResNetResource.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()
        def functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionTestNetResource.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()

        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(CreateCaseParams.with()
                .process(functionTestNet)
                .title("Test")
                .color("")
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["createUser": ["value": "true", "type": "boolean"]]))

        Optional<AbstractUser> userOpt = userService.findUserByUsername("test@test.com", null)
        assert userOpt.isPresent()

        userService.deleteUser(userOpt.get())
        petriNetService.deletePetriNet(DeletePetriNetParams.with()
                .petriNetId(functionResNet.stringId)
                .loggedUser(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build())
        petriNetService.deletePetriNet(DeletePetriNetParams.with()
                .petriNetId(functionTestNet.stringId)
                .loggedUser(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build())
    }

    @Test
    void testProcessFunctions() {
        def functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionTestNetResource.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()
        assert functionTestNet

        Case aCase = workflowService.createCase(CreateCaseParams.with()
                .process(functionTestNet)
                .title("Test")
                .color("")
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["enum": ["value": "ano", "type": "enumeration"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getDataField("number").isRequired(aCase.tasks.first().transition)
        def fieldBehavior = aCase.getDataField("number").behavior
        assert fieldBehavior.containsKey(aCase.tasks.first().transition) && fieldBehavior.get(aCase.tasks.first().transition).contains(FieldBehavior.EDITABLE)

        petriNetService.deletePetriNet(DeletePetriNetParams.with()
                .petriNetId(functionTestNet.stringId)
                .loggedUser(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build())
    }

    @Test
    void testNamespaceFunctionException() {
        assertThrows(NullPointerException.class, () -> {
            def nets = petriNetService.getByIdentifier(FUNCTION_RES_IDENTIFIER, Pageable.unpaged())
            if (nets) {
                nets.each {
                    petriNetService.deletePetriNet(DeletePetriNetParams.with()
                            .petriNetId(it.stringId)
                            .loggedUser(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                            .build())
                }
            }

            def functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(functionTestNetResource.inputStream)
                    .releaseType(VersionType.MAJOR)
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getNet()
            assert functionTestNet

            Case aCase = workflowService.createCase(CreateCaseParams.with()
                    .process(functionTestNet)
                    .title("Test")
                    .color("")
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getCase()
            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        })
    }

    @Test
    void testNewNetVersionMissingMethodException() {
        assertThrows(NullPointerException.class, () -> {
            def nets = petriNetService.getByIdentifier(FUNCTION_TEST_IDENTIFIER, Pageable.unpaged())
            if (nets) {
                nets.each {
                    petriNetService.deletePetriNet(DeletePetriNetParams.with()
                            .petriNetId(it.stringId)
                            .loggedUser(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                            .build())
                }
            }

            def functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(functionTestNetResource.inputStream)
                    .releaseType(VersionType.MAJOR)
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getNet()
            assert functionTestNet

            Case aCase = workflowService.createCase(CreateCaseParams.with()
                    .process(functionTestNet)
                    .title("Test")
                    .color("")
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getCase()
            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["text": ["value": "20", "type": "text"]]))

            functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(functionTestNetResourceV2.inputStream)
                    .releaseType(VersionType.MAJOR)
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getNet()
            assert functionTestNet

            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["text": ["value": "20", "type": "text"]]))
        })
    }

    @Test
    void testProcessFunctionException() {
        assertThrows(MissingMethodException.class, () -> {
            def functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(functionTestNetResource.inputStream)
                    .releaseType(VersionType.MAJOR)
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getNet()
            assert functionTestNet

            Case aCase = workflowService.createCase(CreateCaseParams.with()
                    .process(functionTestNet)
                    .title("Test")
                    .color("")
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build()).getCase()
            dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number3": ["value": "20", "type": "number"]]))
        })
    }

    @Test
    void testNewVersionOfNamespaceFunction() {
        def functionResNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionResNetResource.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()
        def functionTestNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionTestNetResource.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()

        assert functionResNet
        assert functionTestNet

        Case aCase = workflowService.createCase(CreateCaseParams.with()
                .process(functionTestNet)
                .title("Test")
                .color("")
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 + 20

        functionResNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionResNetResourceV2.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()
        assert functionResNet

        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 * 20
    }

    @Test
    void testNamespaceMethodOverloadingFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(functionOverloadingFailNetResource.inputStream)
                    .releaseType(VersionType.MAJOR)
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build())
        })
    }

    @Test
    void testNamespaceUseCaseUpdate() {
        def functionResV2Net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionResNetResourceV2.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()
        def functionTestV2Net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(functionTestNetResourceV2.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()

        Case aCase = workflowService.createCase(CreateCaseParams.with()
                .process(functionTestV2Net)
                .title("Test")
                .color("")
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getCase()
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
            petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(functionOverloadingFailNetResourceV2.inputStream)
                    .releaseType(VersionType.MAJOR)
                    .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                    .build())
        })
    }

    @Test
    void testProcessMethodOverloading() {
        testMethodOverloading(functionOverloadingNetResourceV2)
    }

    private void testMethodOverloading(Resource resource) {
        def petriNet = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(resource.inputStream)
                .releaseType(VersionType.MAJOR)
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getNet()

        assert petriNet

        Case aCase = workflowService.createCase(CreateCaseParams.with()
                .process(petriNet)
                .title("Test")
                .color("")
                .author(ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()))
                .build()).getCase()
        dataService.setData(aCase.tasks.first().task, ImportHelper.populateDataset(["number": ["value": "20", "type": "number"]]))
        aCase = workflowService.findOne(aCase.getStringId())

        assert aCase.getFieldValue("number2") == 20 * 20
        assert aCase.getFieldValue("text") == "20.0 20.0"
    }
}
