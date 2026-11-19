package com.netgrif.application.engine.startup.runner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;

class QuartzSchedulerRunnerTest {

    private Scheduler scheduler;
    private QuartzSchedulerRunner runner;

    @BeforeEach
    void setUp() {
        scheduler = mock(Scheduler.class);
        runner = new QuartzSchedulerRunner(scheduler);
    }

    @Test
    void startsSchedulerWhenRunIsEnabled() throws Exception {
        ReflectionTestUtils.setField(runner, "start", true);

        runner.run(new DefaultApplicationArguments());

        verify(scheduler).start();
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    void doesNothingWhenRunIsDisabled() throws Exception {
        ReflectionTestUtils.setField(runner, "start", false);

        runner.run(new DefaultApplicationArguments());

        verifyNoInteractions(scheduler);
    }

    @Test
    void propagatesSchedulerStartFailure() throws Exception {
        ReflectionTestUtils.setField(runner, "start", true);
        doThrow(new SchedulerException("cannot start")).when(scheduler).start();

        assertThrows(SchedulerException.class, () -> runner.run(new DefaultApplicationArguments()));
    }
}
