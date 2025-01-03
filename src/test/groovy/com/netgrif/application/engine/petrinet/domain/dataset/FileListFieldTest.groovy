package com.netgrif.application.engine.petrinet.domain.dataset

import com.fasterxml.jackson.databind.ObjectMapper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.requestbodies.file.FileFieldRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@TestPropertySource(properties = "nae.storage.minio.enabled=true")
@AutoConfigureMockMvc
class FileListFieldTest {

    public static final String FIELD_ID = "fileList"
    public static final String TASK_TITLE = "Task"
    public static final String USER_EMAIL = "super@netgrif.com"
    public static final String MOCK_FILE_NAME = "hello.txt"

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

    private ObjectMapper objectMapper

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build()
        objectMapper = new ObjectMapper()

    }


    @Test
    void downloadRemoteFile() {
        Case useCase = uploadTestFile()
        def taskPair = useCase.tasks.find { it.transition == "task" }
        assert taskPair != null

        mockMvc.perform(get("/api/task/" + taskPair.task + "/file/named")
                .param("fieldId", FIELD_ID)
                .param("fileName", MOCK_FILE_NAME)
                .with(httpBasic(USER_EMAIL, userPassword))
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Hello")))
                .andReturn()

    }

    @Test
    void deleteRemoteFile() throws Exception {
        Case useCase = uploadTestFile()

        def taskPair = useCase.tasks.find { it.transition == "task" }
        assert taskPair != null

        FileFieldRequest requestBody = new FileFieldRequest(FIELD_ID, taskPair.task, MOCK_FILE_NAME)

        mockMvc.perform(delete("/api/task/" + taskPair.task + "/file/named")
                .content(objectMapper.writeValueAsBytes(requestBody))
                .contentType(MediaType.APPLICATION_JSON)
                .with(httpBasic(USER_EMAIL, userPassword))
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        Assertions.assertThatThrownBy(() ->
                mockMvc.perform(get("/api/task/" + taskPair.task + "/file/named")
                        .param("fieldId", FIELD_ID)
                        .param("fileName", MOCK_FILE_NAME)
                        .with(httpBasic(USER_EMAIL, userPassword))
                ).andDo(print())
        ).isInstanceOf(FileNotFoundException.class)
    }

    @Test
    void getFileByCaseAndName() {
        Case useCase = uploadTestFile()

        IUser user = userService.findByEmail(USER_EMAIL, true)
        assert user != null

        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())

        mockMvc.perform(get("/api/workflow/case/" + useCase.getStringId() + "/file/named")
                .param("fieldId", FIELD_ID)
                .param("fileName", MOCK_FILE_NAME)
                .with(httpBasic(USER_EMAIL, userPassword)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Hello")))
                .andReturn()
    }

    PetriNet getNet() {
        def netOptional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/remoteFileListField.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netOptional.getNet() != null
        return netOptional.getNet()
    }

    private Case uploadTestFile() {
        PetriNet net = getNet()
        IUser user = userService.findByEmail(USER_EMAIL, true)
        assert user != null
        Case useCase = workflowService.createCase(net.getStringId(), "Test file from file list download", "black", user.transformToLoggedUser()).getCase()
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())

        MockMultipartFile file
                = new MockMultipartFile(
                "files",
                MOCK_FILE_NAME,
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        )
        def taskPair = useCase.tasks.find { it.transition == "task" }
        assert taskPair != null

        FileFieldRequest requestBody = new FileFieldRequest(FIELD_ID, taskPair.task, MOCK_FILE_NAME)

        MockMultipartFile data
                = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(requestBody)
        )
        mockMvc.perform(multipart("/api/task/" + taskPair.task + "/files")
                .file(file)
                .file(data)
                .with(httpBasic(USER_EMAIL, userPassword))
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andReturn()
        return useCase
    }
}
