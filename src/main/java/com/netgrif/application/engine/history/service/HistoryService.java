package com.netgrif.application.engine.history.service;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.repository.CreateCaseEventLogRepository;
import com.netgrif.application.engine.history.domain.caseevents.repository.DeleteCaseEventLogRepository;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.SetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.repository.GetDataEventLogRepository;
import com.netgrif.application.engine.history.domain.dataevents.repository.SetDataEventLogRepository;
import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.repository.DeletePetriNetEventLogRepository;
import com.netgrif.application.engine.history.domain.petrinetevents.repository.ImportPetriNetEventLogRepository;
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.repository.AssignTaskEventLogRepository;
import com.netgrif.application.engine.history.domain.taskevents.repository.CancelTaskEventLogRepository;
import com.netgrif.application.engine.history.domain.taskevents.repository.FinishTaskEventLogRepository;
import com.netgrif.application.engine.history.domain.userevents.UserEventLogRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService implements IHistoryService {

    @Autowired
    private UserEventLogRepository userEventLogRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private CreateCaseEventLogRepository createCaseEventLogRepository;

    @Autowired
    private DeleteCaseEventLogRepository deleteCaseEventLogRepository;

    @Autowired
    private SetDataEventLogRepository setDataEventLogRepository;

    @Autowired
    private GetDataEventLogRepository getDataEventLogRepository;

    @Autowired
    private ImportPetriNetEventLogRepository importPetriNetEventLogRepository;

    @Autowired
    private DeletePetriNetEventLogRepository deletePetriNetEventLogRepository;

    @Autowired
    private AssignTaskEventLogRepository assignTaskEventLogRepository;

    @Autowired
    private CancelTaskEventLogRepository cancelTaskEventLogRepository;

    @Autowired
    private FinishTaskEventLogRepository finishTaskEventLogRepository;

    @Override
    public void save(EventLog eventLog) {
        eventLogRepository.save(eventLog);
    }

    @Override
    public List<EventLog> findAllByIds(List<ObjectId> eventIds) {
        return eventLogRepository.findAllById(eventIds);
    }

    @Override
    public List<CreateCaseEventLog> findAllCreateCaseEventLogsByCaseId(ObjectId caseId) {
        return createCaseEventLogRepository.findAllByCaseId(caseId);
    }

    @Override
    public List<DeleteCaseEventLog> findAllDeleteCaseEventLogsByCaseId(ObjectId caseId) {
        return deleteCaseEventLogRepository.findAllByCaseId(caseId);
    }

    @Override
    public List<GetDataEventLog> findAllGetDataEventLogsByCaseId(ObjectId caseId) {
        return getDataEventLogRepository.findAllByCaseId(caseId);
    }

    @Override
    public List<GetDataEventLog> findAllGetDataEventLogsByTaskId(ObjectId taskId) {
        return getDataEventLogRepository.findAllByTaskId(taskId);
    }

    @Override
    public List<SetDataEventLog> findAllSetDataEventLogsByCaseId(ObjectId caseId) {
        return setDataEventLogRepository.findAllByCaseId(caseId);
    }

    @Override
    public List<SetDataEventLog> findAllSetDataEventLogsByTaskId(ObjectId taskId) {
        return setDataEventLogRepository.findAllByTaskId(taskId);
    }

    @Override
    public List<DeletePetriNetEventLog> findAllDeletePetriNetEventLogsByNetId(ObjectId netId) {
        return deletePetriNetEventLogRepository.findAllByNetId(netId);
    }

    @Override
    public List<ImportPetriNetEventLog> findAllImportPetriNetEventLogsByNetId(ObjectId netId) {
        return importPetriNetEventLogRepository.findAllByNetId(netId);
    }

    @Override
    public List<AssignTaskEventLog> findAllAssignTaskEventLogsByTaskId(ObjectId taskId) {
        return assignTaskEventLogRepository.findAllByTaskId(taskId);
    }

    @Override
    public List<AssignTaskEventLog> findAllAssignTaskEventLogsByUserId(String userId) {
        return assignTaskEventLogRepository.findAllByUserId(userId);
    }

    @Override
    public List<CancelTaskEventLog> findAllCancelTaskEventLogsByTaskId(ObjectId taskId) {
        return cancelTaskEventLogRepository.findAllByTaskId(taskId);
    }

    @Override
    public List<CancelTaskEventLog> findAllCancelTaskEventLogsByUserId(String userId) {
        return cancelTaskEventLogRepository.findAllByUserId(userId);
    }

    @Override
    public List<FinishTaskEventLog> findAllFinishTaskEventLogsByTaskId(ObjectId taskId) {
        return finishTaskEventLogRepository.findAllByTaskId(taskId);
    }

    @Override
    public List<FinishTaskEventLog> findAllFinishTaskEventLogsByUserId(String userId) {
        return finishTaskEventLogRepository.findAllByUserId(userId);
    }
}