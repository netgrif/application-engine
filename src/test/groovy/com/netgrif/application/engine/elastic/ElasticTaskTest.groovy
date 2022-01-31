package com.netgrif.application.engine.elastic

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository
import com.netgrif.application.engine.elastic.service.ReindexingTask
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository
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
        classes = ApplicationEngine.class
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
        assert optional.getNet() != null

        def net = optional.getNet()
        10.times {
            helper.createCase("Case $it", net)
        }

        reindexingTask.forceReindexPage(QCase.case$.lastModified.before(LocalDateTime.now()), 0, 1)
    }
}