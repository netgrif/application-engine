package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@SpringBootTest
@ActiveProfiles(["test"])
@RunWith(SpringRunner.class)
class DynamicValidationPerformanceTest {

    public static final Logger log = LoggerFactory.getLogger(DynamicValidationPerformanceTest)

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IWorkflowService workflowService

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testValidations() {
        Optional<PetriNet> optNet1 = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_validations_performance_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        Optional<PetriNet> optNet2 = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_validations_performance_test_comparison.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())

        def aCase1 = importHelper.createCase("Case 1", optNet1.get())
        def aCase2 = importHelper.createCase("Case 2", optNet2.get())

        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
    }

    Map<String, Field> getData(Case useCase) {
        Task task = task(useCase)
        return dataService.getData(task, useCase).collectEntries { [(it.importId): (it)] }
    }

    Task task(Case useCase) {
        return taskService.findOne(useCase.tasks.find { it.transition == "transition" }.task)
    }

    void run(Case first, Case second) {
        LocalDateTime pre1 = LocalDateTime.now()
        getData(first)
        LocalDateTime post1 = LocalDateTime.now()

        LocalDateTime pre2 = LocalDateTime.now()
        getData(second)
        LocalDateTime post2 = LocalDateTime.now()

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS")
        log.info("With dynamic validations: ${pre1.format(format)} - ${post1.format(format)} = ${ChronoUnit.MILLIS.between(pre1, post1)}ms")
        log.info("With static validations: ${pre2.format(format)} - ${post2.format(format)} = ${ChronoUnit.MILLIS.between(pre2, post2)}ms")
    }
}
