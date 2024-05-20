package com.netgrif.application.engine.workflow.service;


import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.startup.SystemUserRunner;
import com.netgrif.application.engine.startup.UriRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class VisualIdTest {

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SystemUserRunner userRunner;

    @Autowired
    private UriRunner uriRunner;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    PetriNet net;

    @BeforeEach
    public void setUp() throws Exception {
        mongoTemplate.getDb().drop();
        taskRepository.deleteAll();
        userRunner.run("");
        uriRunner.run();

        petriNetService.importPetriNet(new FileInputStream("src/test/resources/prikladFM.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        net = petriNetRepository.findAll().get(0);
        assert net != null;
    }

    @Test
//    @RepeatedTest(100)
    public void testGenerateVisualIds() throws InterruptedException {
        ConcurrentHashMap<String, Integer> ids = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(100);

        long startTime = System.nanoTime();

        IntStream.range(0, 1000000).forEach(i -> executor.submit(() -> {
            Case caseInstance = new Case(net);
            String visualId = caseInstance.getVisualId();
            ids.put(visualId, ids.getOrDefault(visualId, 0) + 1);
        }));

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);

        long duplicates = ids.values().stream().filter(count -> count > 1).count();
        System.out.println("Total duplicates: " + duplicates);
        System.out.println("Time: " + duration + " seconds");
        assert duplicates == 0 : "There should be no duplicates";
    }

}
