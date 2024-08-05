package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@AutoConfigureMockMvc
class FileFieldTest {

    public static final String FIELD_ID = "file"
    public static final String TASK_TITLE = "Task"

    @Autowired
    private SuperAdminConfiguration configuration

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private Importer importer

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IUserService userService

    @Autowired
    private WebApplicationContext context

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    private MockMvc mockMvc

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build()
    }

    PetriNet getNet() {
        def netOptional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/remoteFileField.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        assert netOptional.getNet() != null
        return netOptional.getNet()
    }

    @Test
    void testRemoteAttribute() {
        PetriNet net = getNet()
        assert net.getField(FIELD_ID).isPresent()
        assert (net.getField(FIELD_ID).get() as FileField).isRemote()
    }

    @Test
    void downloadFileByCase() {
        PetriNet net = getNet()

        IUser user = userService.findByEmail(configuration.email, true)
        assert user != null

        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test file download")
                .color("black")
                .loggedUser(user.transformToLoggedUser())
                .build()
        Case useCase = workflowService.createCase(createCaseParams).getCase()
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())

        mockMvc.perform(get("/api/workflow/case/" + useCase.getStringId() + "/file")
                .param("fieldId", FIELD_ID)
                .with(httpBasic(configuration.email, configuration.password)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Netgrif")))
                .andReturn()
    }

    @Test
    void downloadFileByTask() {
        PetriNet net = getNet()

        IUser user = userService.findByEmail(configuration.email, true)
        assert user != null

        CreateCaseParams createCaseParams = CreateCaseParams.builder()
                .petriNet(net)
                .title("Test file download")
                .color("black")
                .loggedUser(user.transformToLoggedUser())
                .build()
        Case useCase = workflowService.createCase(createCaseParams).getCase()
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())

        mockMvc.perform(get("/api/task/" + importHelper.getTaskId(TASK_TITLE, useCase.getStringId()) + "/file")
                .param("fieldId", FIELD_ID)
                .with(httpBasic(configuration.email, configuration.password)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Netgrif")))
                .andReturn()
    }


}
