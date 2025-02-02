package com.netgrif.application.engine.history.service.listener;

import com.netgrif.application.engine.event.dispatchers.DataDispatcher;
import com.netgrif.application.engine.event.dispatchers.common.AbstractDispatcher;
import com.netgrif.application.engine.event.events.data.GetDataEvent;
import com.netgrif.application.engine.event.events.data.SetDataEvent;
import com.netgrif.application.engine.event.listeners.Listener;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.SetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.repository.GetDataEventLogRepository;
import com.netgrif.application.engine.history.domain.dataevents.repository.SetDataEventLogRepository;
import org.springframework.stereotype.Component;

import java.util.EventObject;
import java.util.Set;

@Component
public class DataEventListener extends Listener {
    private final GetDataEventLogRepository getDataEventLogRepository;
    private final SetDataEventLogRepository setDataEventLogRepository;

    public DataEventListener(DataDispatcher dispatcher,
                             GetDataEventLogRepository getDataEventLogRepository,
                             SetDataEventLogRepository setDataEventLogRepository) {
        this.getDataEventLogRepository = getDataEventLogRepository;
        this.setDataEventLogRepository = setDataEventLogRepository;
        this.registerAll(dispatcher,
                Set.of(SetDataEvent.class,
                        GetDataEvent.class),
                AbstractDispatcher.DispatchMethod.ASYNC);
    }

    @Override
    public void onAsyncEvent(EventObject event, AbstractDispatcher dispatcher) {
        if (event instanceof SetDataEvent) {
            setDataEventLogRepository.save(SetDataEventLog.fromEvent((SetDataEvent) event));
        } else if (event instanceof GetDataEvent) {
            getDataEventLogRepository.save(GetDataEventLog.fromEvent((GetDataEvent) event));
        }
    }

    @Override
    public void onEvent(EventObject event, AbstractDispatcher dispatcher) {
        //do nothing
    }
}
