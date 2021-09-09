package com.netgrif.workflow.elastic

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.WorkflowManagementSystemApplication
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository
import com.netgrif.workflow.elastic.service.ReindexingTask
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.QCase
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDateTime

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WorkflowManagementSystemApplication.class
)
@AutoConfigureMockMvc
class ElasticTaskTest {

    @Autowired
    private ElasticTaskRepository elasticTaskRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private TestHelper testHelper
    @Autowired
    private ImportHelper helper
    @Autowired
    private ReindexingTask reindexingTask
    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator


    @Value("classpath:task_reindex_test.xml")
    private Resource netResource

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void taskReindexTest() {
        def optional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert optional.isPresent()

        def net = optional.get()
        10.times {
            helper.createCase("Case $it", net)
        }

        reindexingTask.forceReindexPage(QCase.case$.lastModified.before(LocalDateTime.now()), 0, 1)
    }
}