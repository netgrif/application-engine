package com.netgrif.application.engine.workflow;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

@Slf4j
@Disabled
@SpringBootTest
@RequiredArgsConstructor
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkflowPerformanceTest {

    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IDataService dataService;
    private final IPetriNetService petriNetService;
    private final SuperCreatorRunner superCreatorRunner;
    private final TestHelper testHelper;

    @BeforeEach
    public void beforeEach() {
        testHelper.truncateDbs();
    }

    @Test
    public void testCreatePerformance() throws IOException, MissingPetriNetMetaDataException {
        PetriNet net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/test_create_case_performance.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreatorRunner.getLoggedSuper())
                .build()).getNet();
        iterateAndShowAvgTime("createCase", () -> workflowService.createCase(net.getStringId(), null, null,
                superCreatorRunner.getLoggedSuper(), Locale.getDefault()), 100);
    }

    @Test
    public void testAssignPerformance() {

    }

    @Test
    public void testCancelPerformance() {

    }

    @Test
    public void testFinishPerformance() {

    }

    @Test
    public void testSetDataPerformance() {

    }

    private void iterateAndShowAvgTime(String event, Runnable callback, int iterations) {
        long totalElapsedTime = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            callback.run();
            long finish = System.currentTimeMillis();
            totalElapsedTime += finish - start;
        }
        log.info("AVG time for event [{}] is [{} ms] for [{}] iterations", event, totalElapsedTime / iterations, iterations);
    }
}
