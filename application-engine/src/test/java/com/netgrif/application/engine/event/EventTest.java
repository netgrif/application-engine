package com.netgrif.application.engine.event;


import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.event.dispatchers.CaseDispatcher;
import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.objects.event.events.workflow.CreateCaseEvent;
import com.netgrif.application.engine.objects.event.listeners.Listener;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.EventObject;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class EventTest {

    @Autowired
    private TestHelper helper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private SuperCreatorRunner superCreator;

    @Autowired
    private CaseDispatcher caseDispatcher;

    private PetriNet net;

    @BeforeEach
    void beforeEach() {
        helper.truncateDbs();
        Optional<PetriNet> netOptional = importHelper.createNet("all_data.xml");
        assertTrue(netOptional.isPresent());
        this.net = netOptional.get();
    }


    @Test
    void testCreateCaseEventMultiplicity() {
        AtomicInteger preEventsCounter = new AtomicInteger(0);
        AtomicInteger postEventsCounter = new AtomicInteger(0);
        AtomicReference<Object> createCaseEventRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<Throwable> asyncException = new AtomicReference<>();

        Listener listener = new Listener() {
            @Override
            public <E extends EventObject> void onEvent(E event, AbstractDispatcher dispatcher) {

            }

            @Override
            public <E extends EventObject> void onAsyncEvent(E event, AbstractDispatcher dispatcher) {
                try {
                    createCaseEventRef.set(event);
                    CreateCaseEvent createCaseEvent = (CreateCaseEvent) event;
                    if (createCaseEvent.getEventPhase() == EventPhase.PRE) {
                        preEventsCounter.incrementAndGet();
                    } else {
                        postEventsCounter.incrementAndGet();
                    }
                } catch (Throwable e) {
                    asyncException.set(e);
                } finally {

                    latch.countDown();
                }
            }
        };
        listener.register(caseDispatcher, CreateCaseEvent.class, AbstractDispatcher.DispatchMethod.ASYNC);
        CreateCaseParams params = CreateCaseParams.with()
                .process(net)
                .author(superCreator.getLoggedSuper())
                .build();
        workflowService.createCase(params);

        boolean completed = false;
        try {
            completed = latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted while waiting for async events to complete");
        }
        assertTrue(completed, "Async events did not complete within timeout");

        if (asyncException.get() != null) {
            fail("Exception in async event handler: " + asyncException.get().getMessage(), asyncException.get());
        }

        Object eventObj = createCaseEventRef.get();
        assertNotNull(eventObj, "Expected non-null event object");
        assertEquals(CreateCaseEvent.class, eventObj.getClass(), "Expected CreateCaseEvent class");
        assertNotNull(((CreateCaseEvent) eventObj).getEventPhase(), "Expected non-null Phase Enum");
        assertEquals(1, preEventsCounter.get(), "Expected exactly one PRE phase event");
        assertEquals(1, postEventsCounter.get(), "Expected exactly one POST phase event");
        caseDispatcher.unregisterListener(listener, CreateCaseEvent.class, AbstractDispatcher.DispatchMethod.ASYNC);
    }
}
