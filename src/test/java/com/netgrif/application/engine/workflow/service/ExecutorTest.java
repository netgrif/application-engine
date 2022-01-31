package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.elastic.service.executors.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Random;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ExecutorTest {

    public static final Logger log = LoggerFactory.getLogger(ExecutorTest.class);

    @Autowired
    private Executor executors;

    @BeforeEach
    public void setUp() {

    }

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
