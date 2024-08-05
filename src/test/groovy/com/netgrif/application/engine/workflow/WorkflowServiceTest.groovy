package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static com.netgrif.application.engine.workflow.domain.params.CreateCaseParams.*

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class WorkflowServiceTest {

    public static final String NET_FILE = "case_search_test.xml"
    public static final String CASE_LOCALE_NET_FILE = "create_case_locale.xml"
    public static final String FIRST_AUTO_NET_FILE = "petriNets/NAE_1382_first_trans_auto.xml"
    public static final String SECOND_AUTO_NET_FILE = "petriNets/NAE_1382_first_trans_auto_2.xml"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    private Closure<InputStream> stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    @Disabled
    void testblabla() {
        def testNetWithTriggers = petriNetService.importPetriNet(stream("petriNets/test_with_triggers.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        def testNet = petriNetService.importPetriNet(stream("petriNets/mortgage.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        int iterations = 2
        def paramsWithoutTrigger = CreateCaseParams.builder()
                .petriNet(testNet)
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        def paramsWithTrigger = CreateCaseParams.builder()
                .petriNet(testNetWithTriggers)
                .loggedUser(superCreator.getLoggedSuper())
                .build()

        long totalWithoutTriggers = 0
        (0..iterations).each {
            Date startTime = new Date()
            workflowService.createCase(paramsWithoutTrigger)
            Date endTime = new Date()
            TimeDuration elapsedTimeTransactional = TimeCategory.minus( endTime, startTime )
            totalWithoutTriggers += elapsedTimeTransactional.toMilliseconds()
        }

        long totalWithTriggers = 0
//        (0..iterations).each {
//            Date startTime = new Date()
//            workflowService.createCase(paramsWithTrigger)
//            Date endTime = new Date()
//            TimeDuration elapsedTimeTransactional = TimeCategory.minus( endTime, startTime )
//            totalWithTriggers += elapsedTimeTransactional.toMilliseconds()
//        }

        println("AVG without triggers for 1 create case: " + totalWithoutTriggers / iterations + "ms")
        println("AVG with triggers for 1 create case: " + totalWithTriggers / iterations + "ms")
    }

    @Test
    void testFindOneImmediateData() {
        def testNet = petriNetService.importPetriNet(stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null
        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getImmediateData().size() == 5

        def newCase = workflowService.findOne(aCase.stringId)

        assert newCase.getImmediateData() != null
        assert newCase.getImmediateData().size() == 5
    }

    @Test
    void testFirstTransitionAuto() {
        def testNet = petriNetService.importPetriNet(stream(FIRST_AUTO_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert testNet

        def net = testNet
        CreateCaseParams createCaseParams = builder()
                .petriNet(net)
                .title("autoErr")
                .color("red")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case aCase = workflowService.createCase(createCaseParams).getCase()
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void testSecondTransitionAuto() {
        def net = petriNetService.importPetriNet(stream(SECOND_AUTO_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        CreateCaseParams createCaseParams = builder()
                .petriNet(net)
                .title("autoErr")
                .color("red")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case aCase = workflowService.createCase(createCaseParams).getCase()
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getLoggedSuper())

        importHelper.assignTask("Manuel", aCase.getStringId(), superCreator.getLoggedSuper())
        importHelper.finishTask("Manuel", aCase.getStringId(), superCreator.getLoggedSuper())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void createCaseWithLocale() {
        def testNet = petriNetService.importPetriNet(stream(CASE_LOCALE_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert testNet.getNet() != null

        def net = testNet.getNet()
        CreateCaseParams createCaseParams = builder()
                .petriNet(net)
                .title(null)
                .color(null)
                .loggedUser(superCreator.getLoggedSuper())
                .locale(new Locale('sk'))
                .build()
        Case aCase = workflowService.createCase(createCaseParams).getCase()

        assert aCase.title == "Slovenský preklad"
        assert workflowService.findOne(aCase.stringId).uriNodeId == net.uriNodeId

        createCaseParams = builder()
                .petriNet(net)
                .title(null)
                .color(null)
                .loggedUser(superCreator.getLoggedSuper())
                .locale(new Locale('en'))
                .build()
        Case enCase = workflowService.createCase(createCaseParams).getCase()

        assert enCase.title == "English translation"
    }
}
