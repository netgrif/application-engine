package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.dispatchers.ProcessDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeleteEvent;
import com.netgrif.application.engine.event.events.petrinet.ProcessDeployEvent;
import com.netgrif.application.engine.event.listeners.Listener;
import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.repository.DeletePetriNetEventLogRepository;
import com.netgrif.application.engine.history.domain.petrinetevents.repository.ImportPetriNetEventLogRepository;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.Set;

@Component
public class PetriNetListener extends Listener {

    private final DeletePetriNetEventLogRepository deletePetriNetEventLogRepository;
    private final ImportPetriNetEventLogRepository importPetriNetEventLogRepository;

    public PetriNetListener(ProcessDispatcher dispatcher,
                            DeletePetriNetEventLogRepository deletePetriNetEventLogRepository,
                            ImportPetriNetEventLogRepository importPetriNetEventLogRepository) {
        this.deletePetriNetEventLogRepository = deletePetriNetEventLogRepository;
        this.importPetriNetEventLogRepository = importPetriNetEventLogRepository;
        this.registerAll(dispatcher,
                Set.of(ProcessDeployEvent.class,
                        ProcessDeleteEvent.class),
                AbstractDispatcher.DispatchMethod.ASYNC);
    }

    @Override
    public void onAsyncEvent(EventObject event, AbstractDispatcher dispatcher) {
        if (event instanceof ProcessDeployEvent) {
            importPetriNetEventLogRepository.save(ImportPetriNetEventLog.fromEvent((ProcessDeployEvent) event));
        } else if(event instanceof ProcessDeleteEvent) {
            deletePetriNetEventLogRepository.save(DeletePetriNetEventLog.fromEvent((ProcessDeleteEvent) event));
        }
    }

    @Override
    public void onEvent(EventObject event, AbstractDispatcher dispatcher) {
        // do nothing
    }
}
