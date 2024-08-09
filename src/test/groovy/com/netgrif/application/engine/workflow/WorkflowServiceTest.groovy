package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.params.ImportPetriNetParams
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
    void testFindOneImmediateData() {
        def testNet = petriNetService.importPetriNet(new ImportPetriNetParams(
                stream(NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert testNet.getNet() != null
        Case aCase = importHelper.createCase("Case 1", testNet.getNet())

        assert aCase.getImmediateData().size() == 5

        def newCase = workflowService.findOne(aCase.stringId)

        assert newCase.getImmediateData() != null
        assert newCase.getImmediateData().size() == 5
    }

    @Test
    void testFirstTransitionAuto() {
        def testNet = petriNetService.importPetriNet(new ImportPetriNetParams(
                stream(FIRST_AUTO_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())).getNet()
        assert testNet

        def net = testNet
        CreateCaseParams createCaseParams = with()
                .petriNet(net)
                .title("autoErr")
                .color("red")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case aCase = workflowService.createCase(createCaseParams).getCase()
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getSuperUser())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getSuperUser())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void testSecondTransitionAuto() {
        def net = petriNetService.importPetriNet(new ImportPetriNetParams(
                stream(SECOND_AUTO_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())).getNet()

        CreateCaseParams createCaseParams = with()
                .petriNet(net)
                .title("autoErr")
                .color("red")
                .loggedUser(superCreator.getLoggedSuper())
                .build()
        Case aCase = workflowService.createCase(createCaseParams).getCase()
        importHelper.assignTask("Manual", aCase.getStringId(), superCreator.getSuperUser())
        importHelper.finishTask("Manual", aCase.getStringId(), superCreator.getSuperUser())

        importHelper.assignTask("Manuel", aCase.getStringId(), superCreator.getSuperUser())
        importHelper.finishTask("Manuel", aCase.getStringId(), superCreator.getSuperUser())

        assert workflowService.findOne(aCase.stringId).getActivePlaces().containsKey("p3")
        assert workflowService.findOne(aCase.stringId).getActivePlaces().size() == 1
    }

    @Test
    void createCaseWithLocale() {
        def testNet = petriNetService.importPetriNet(new ImportPetriNetParams(
                stream(CASE_LOCALE_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper()))
        assert testNet.getNet() != null

        def net = testNet.getNet()
        CreateCaseParams createCaseParams = with()
                .petriNet(net)
                .title(null)
                .color(null)
                .loggedUser(superCreator.getLoggedSuper())
                .locale(new Locale('sk'))
                .build()
        Case aCase = workflowService.createCase(createCaseParams).getCase()

        assert aCase.title == "Slovenský preklad"
        assert workflowService.findOne(aCase.stringId).uriNodeId == net.uriNodeId

        createCaseParams = with()
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
