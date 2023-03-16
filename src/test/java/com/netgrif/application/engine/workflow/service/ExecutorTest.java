package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.executors.Executor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Random;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ExecutorTest {

    @Autowired
    private Executor executors;

    @BeforeEach
    public void setUp() {

    }

    // TODO: NAE-1645 what does it test? is sleep necessary?
    @Test
    public void runMoreThanCapacityExecutors() throws InterruptedException {
        for (int i = 0; i < 500; i++) {
            final String id = "EXE" + i;
            executors.execute(id, () -> {
                log.info("[" + id + "]Running task in executor");
                try {
                    Thread.sleep(new Random().nextInt(500));
                } catch (InterruptedException e) {
                    log.error("ExecutorTest failed: ", e);
                }
                log.info("[" + id + "]Ending task in executor");
            });
        }
        Thread.sleep(10000);
    }
}
