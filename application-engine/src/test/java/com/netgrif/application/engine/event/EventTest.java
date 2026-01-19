package com.netgrif.application.engine.event;


import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.event.dispatchers.CaseDispatcher;
import com.netgrif.application.engine.objects.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.objects.event.events.workflow.CaseEvent;
import com.netgrif.application.engine.objects.event.events.workflow.CreateCaseEvent;
import com.netgrif.application.engine.objects.event.listeners.Listener;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void beforeAll() {
        helper.truncateDbs();
        Optional<PetriNet> netOptional = importHelper.createNet("all_data.xml");
        assert netOptional.isPresent();
        this.net = netOptional.get();
    }


    @Test
    void testCreateCaseEventMultiplicity() {
        List<CreateCaseEvent> casesPreEvents = new ArrayList<>();
        List<CreateCaseEvent> casesPostEvents = new ArrayList<>();
        Listener listener = new Listener() {
            @Override
            public <E extends EventObject> void onEvent(E event, AbstractDispatcher dispatcher) {

            }

            @Override
            public <E extends EventObject> void onAsyncEvent(E event, AbstractDispatcher dispatcher) {
                assertEquals(CreateCaseEvent.class, event.getClass());
                CreateCaseEvent createCaseEvent = (CreateCaseEvent) event;
                assertNotNull(createCaseEvent);
                assertNotNull(createCaseEvent.getEventPhase());
                if (createCaseEvent.getEventPhase()==EventPhase.PRE) {
                    casesPreEvents.add(createCaseEvent);
                } else {
                    casesPostEvents.add(createCaseEvent);
                }

            }
        };
        listener.register(caseDispatcher, CreateCaseEvent.class, AbstractDispatcher.DispatchMethod.ASYNC);
        workflowService.createCase(net.getStringId(), null, null, superCreator.getLoggedSuper());

        assertEquals(1, casesPreEvents.size(), "Expected exactly one PRE phase event");
        assertEquals(1, casesPostEvents.size(), "Expected exactly one POST phase event");
    }
}
