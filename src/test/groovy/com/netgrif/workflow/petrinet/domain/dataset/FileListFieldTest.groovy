package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@AutoConfigureMockMvc
class FileListFieldTest {

    public static final String FIELD_ID = "fileList"
    public static final String TASK_TITLE = "Task"
    public static final String USER_EMAIL = "super@netgrif.com"

    @Value('${admin.password:password}')
    private String userPassword

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
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    private MockMvc mockMvc

    @Before
    void setup() {
        testHelper.truncateDbs()
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build()
    }

    PetriNet getNet() {
        def netOptional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/remoteFileListField.xml"), "major", superCreator.getLoggedSuper())
        assert netOptional.isPresent()
        return netOptional.get()
    }

    @Test
    void testRemoteAttribute() {
        PetriNet net = getNet()
        assert net.getField(FIELD_ID).isPresent()
        assert net.getField(FIELD_ID).get().isRemote()
    }

    @Test
    void downloadFileByCaseAndName() {
        PetriNet net = getNet()

        User user = userService.findByEmail(USER_EMAIL, true)
        assert user != null

        Case useCase = workflowService.createCase(net.getStringId(), "Test file from file list download", "black", user.transformToLoggedUser())
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())

        mockMvc.perform(get("/api/workflow/case/" + useCase.getStringId() + "/file/" + FIELD_ID + '/test-file.txt')
                .with(httpBasic(USER_EMAIL, userPassword)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Netgrif")))
                .andReturn()
    }

    @Test
    void downloadFileByTask() {
        PetriNet net = getNet()

        User user = userService.findByEmail(USER_EMAIL, true)
        assert user != null

        Case useCase = workflowService.createCase(net.getStringId(), "Test file from file list download", "black", user.transformToLoggedUser())
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())

        mockMvc.perform(get("/api/task/" + importHelper.getTaskId(TASK_TITLE, useCase.getStringId()) + "/file/" + FIELD_ID + '/test-file-list.txt').
                with(httpBasic(USER_EMAIL, userPassword)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Netgrif")))
                .andReturn()
    }
}
