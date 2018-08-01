package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.service.interfaces.IUserService
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.WorkflowController
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
@AutoConfigureMockMvc
class FileFieldTest {

    public static final String NET_ID = "remote_file_field_net"
    public static final String FIELD_ID = "file"
    public static final String TASK_TITLE = "Task"

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private TestHelper testHelper

    @Autowired
    private Importer importer

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IUserService userService

    @Before
    void setup() {
        testHelper.truncateDbs()
    }

    PetriNet getNet() {
        def netOptional = importer.importPetriNet(FileField.getClassLoader().getResourceAsStream("remoteFileField.xml"), "Remote file test", "RFT")
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
    void downloadFile() {
        testHelper.truncateDbs()
        PetriNet net = getNet()

        User user = userService.findByEmail("super@netgrif.com", true)
        assert user != null

        Case useCase = workflowService.createCase(net.getStringId(), "Test file download", "black", user.transformToLoggedUser())
        importHelper.assignTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())
//        importHelper.setTaskData(TASK_TITLE, useCase.getStringId(), ["file": ["type": "file", "value": "https://google.com"]])
//        importHelper.finishTask(TASK_TITLE, useCase.getStringId(), user.transformToLoggedUser())


        MvcResult result = mockMvc.perform(get("/api/workflow/case/" + useCase.getStringId() + "/file/" + FIELD_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
        assert result.response.contentType == MediaType.APPLICATION_OCTET_STREAM_VALUE
    }


}
