package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Slf4j
@SpringBootTest
@ActiveProfiles(["test"])
@CompileStatic
@ExtendWith(SpringExtension.class)
class DynamicValidationPerformanceTest extends EngineTest {

    @BeforeEach
    void before() {
        truncateDbs()
    }

    @Test
    void testValidations() {
        ImportPetriNetEventOutcome optNet1 = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_validations_performance_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        ImportPetriNetEventOutcome optNet2 = petriNetService.importPetriNet(new FileInputStream("src/test/resources/petriNets/dynamic_validations_performance_test_comparison.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())

        def aCase1 = importHelper.createCase("Case 1", optNet1.getNet())
        def aCase2 = importHelper.createCase("Case 2", optNet2.getNet())

        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
        run(aCase1, aCase2)
    }

    Map<String, Field> getData(Case useCase) {
        Task task = task(useCase)
        return dataService.getData(task, useCase, superCreator.getSuperUser()).getData().collectEntries { [(it.fieldId): (it)] }
    }

    Task task(Case useCase) {
        return taskService.findOne(useCase.getTaskStringId("transition"))
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
