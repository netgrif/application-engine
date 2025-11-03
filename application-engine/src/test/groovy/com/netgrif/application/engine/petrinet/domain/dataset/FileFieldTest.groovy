package com.netgrif.application.engine.petrinet.domain.dataset

import com.fasterxml.jackson.databind.ObjectMapper
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.files.minio.MinIoHostInfo
import com.netgrif.application.engine.files.minio.StorageConfigurationProperties
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.objects.auth.constants.UserConstants
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.requestbodies.file.FileFieldRequest
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
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
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.core.StringContains.containsString
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@TestPropertySource(properties = "netgrif.engine.storage.minio.enabled=true")
@AutoConfigureMockMvc
class FileFieldTest {

    public static final String FIELD_ID = "file"
    public static final String TASK_TITLE = "Task"
    public static final String USER_EMAIL = "super@netgrif.com"
    public static final String MOCK_FILE_NAME = "hello.txt"

    public static final String BUCKET = "default"

    static MinioClient mc;

    @Value('${netgrif.engine.security.auth.admin-password:password}')
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
    private UserService userService

    @Autowired
    private WebApplicationContext context

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreatorRunner superCreator

    @Autowired
    private StorageConfigurationProperties.MinIoStorageProperties MinIoStorageProperties

    private MockMvc mockMvc

    private ObjectMapper objectMapper

    private Authentication auth

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()

        MinIoHostInfo hostInfo = MinIoStorageProperties.getHosts("host_1")

        mc = MinioClient.builder()
                .endpoint(hostInfo.host)
                .credentials(hostInfo.user, hostInfo.password)
                .build();

        boolean exists = mc.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
        if (!exists) {
            mc.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build()
        objectMapper = new ObjectMapper()

        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        def adminUser = importHelper.createUser(new User(firstName: "Admin", lastName: "User", username: UserConstants.ADMIN_USER_USERNAME, email: UserConstants.ADMIN_USER_EMAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("admin")] as Authority[],
//                [] as Group[],
                [] as ProcessRole[])

        auth = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(adminUser), "password", [auths.get("admin")] as List<AuthorityImpl>)
        auth.setDetails(new WebAuthenticationDetails(new MockHttpServletRequest()))
    }

    PetriNet getNet() {
        def netOptional = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/remoteFileField.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert netOptional.getNet() != null
        return netOptional.getNet()
    }

    @Test
    void downloadFileByCase() {
        Case useCase = uploadTestFile()

        AbstractUser user = userService.findUserByUsername(UserConstants.ADMIN_USER_USERNAME, null).get()
        assert user != null

        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), ActorTransformer.toLoggedUser(user))

        mockMvc.perform(get("/api/workflow/case/" + useCase.getStringId() + "/file")
                .param("fieldId", FIELD_ID)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Hello")))
                .andReturn()
    }

    @Test
    void downloadFileByTask() {
        Case useCase = uploadTestFile()

        AbstractUser user = userService.findUserByUsername(UserConstants.ADMIN_USER_USERNAME, null).get()
        assert user != null

        def taskPair = useCase.tasks.find { it.transition == "task" }
        assert taskPair != null

        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), ActorTransformer.toLoggedUser(user))

        mockMvc.perform(get("/api/task/" + taskPair.task + "/file")
                .param("fieldId", FIELD_ID)
                .with(csrf().asHeader())
                .with(authentication(this.auth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(containsString("Hello")))
                .andReturn()
    }

    @Test
    void deleteRemoteFile() {
        Case useCase = uploadTestFile()

        def taskPair = useCase.tasks.find { it.transition == "task" }
        assert taskPair != null

        FileFieldRequest requestBody = new FileFieldRequest(FIELD_ID, taskPair.task, MOCK_FILE_NAME)


        mockMvc.perform(delete("/api/task/" + taskPair.task + "/file")
                .content(objectMapper.writeValueAsBytes(requestBody))
                .contentType(APPLICATION_JSON)
                .with(csrf().asHeader())
                .with(authentication(this.auth))
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        Assertions.assertThatThrownBy(() ->
                mockMvc.perform(get("/api/task/" + taskPair.task + "/file")
                        .param("fieldId", FIELD_ID)
                        .with(csrf().asHeader())
                        .with(authentication(this.auth))
                ).andDo(print())
        ).isInstanceOf(FileNotFoundException.class)
    }

    private Case uploadTestFile() {
        PetriNet net = getNet()
        AbstractUser user = userService.findUserByUsername(UserConstants.ADMIN_USER_USERNAME, null).get()
        assert user != null
        Case useCase = workflowService.createCase(CreateCaseParams.with()
                .process(net)
                .title("Test file from file list download")
                .color("black")
                .author(ActorTransformer.toLoggedUser(user))
                .build()).getCase()
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), ActorTransformer.toLoggedUser(user))

        MockMultipartFile file
                = new MockMultipartFile(
                "file",
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
        mockMvc.perform(multipart("/api/task/" + taskPair.task + "/file")
                .file(file)
                .file(data)
                .with(csrf().asHeader())
                .with(authentication(this.auth))
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andReturn()
        return useCase
    }
}
