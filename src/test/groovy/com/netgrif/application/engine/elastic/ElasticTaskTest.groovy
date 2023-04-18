package com.netgrif.application.engine.elastic

import com.netgrif.application.engine.ApplicationEngine
import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.elastic.service.ReindexingTask
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.workflow.domain.QCase
import groovy.transform.CompileStatic
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
@CompileStatic
class ElasticTaskTest extends EngineTest {

    @Autowired
    private ReindexingTask reindexingTask

    @Value("classpath:task_reindex_test.xml")
    private Resource netResource

    @BeforeEach
    void before() {
        truncateDbs()
    }

    @Test
    void taskReindexTest() {
        def optional = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert optional.getNet() != null

        def net = optional.getNet()
        10.times {
            importHelper.createCase("Case $it", net)
        }

        reindexingTask.forceReindexPage(QCase.case$.lastModified.before(LocalDateTime.now()), 0, 1)
    }
}