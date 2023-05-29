package com.netgrif.application.engine.elastic

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.elastic.service.ReindexingTask
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(["test"])
@SpringBootTest
@ExtendWith(SpringExtension.class)
class ReindexTest {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    protected IElasticCaseService elasticCaseService

    @Autowired
    private ReindexingTask reindexingTask

    @Autowired
    TestHelper testHelper

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void reindexTest() {
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        int countTread = Thread.activeCount()
        List<Thread> threads = []
        List<Case> savedCase = []
        for (int i in 1..2000) {
            threads << Thread.start {
                def useCase = workflowService.createCase(net.getNet().stringId, "Test", "color", superCreator.getLoggedSuper()).getCase()
                savedCase.add(useCase)
            }
        }
        threads.each { it.join() }
        reindexingTask.reindex()

        assert Thread.activeCount() - countTread < 550

        threads = []
        for (int i in 1..4000) {
            threads << Thread.start {
                def useCase = workflowService.createCase(net.getNet().stringId, "Test", "color", superCreator.getLoggedSuper()).getCase()
                savedCase.add(useCase)
            }
        }
        threads.each { it.join() }
        reindexingTask.reindex()

        assert Thread.activeCount() - countTread < 550

        Thread.sleep(15000)

        assert Thread.activeCount() - countTread < 550

        savedCase.forEach(it -> {
            CaseSearchRequest request = new CaseSearchRequest()
            request.query = "stringId:\"" + it.getStringId() + "\""
            List<Case> result = elasticCaseService.search(Collections.singletonList(request), superCreator.getLoggedSuper(), PageRequest.of(0, 10), LocaleContextHolder.getLocale(), false).getContent()
            assert result.size() == 1
        })


    }

}
