package com.netgrif.application.engine.history.service;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import com.netgrif.application.engine.history.domain.dataevents.SetDataEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog;
import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import org.bson.types.ObjectId;

import java.util.List;

public interface IHistoryService {

    void save(EventLog eventLog);

    List<EventLog> findAllByIds(List<ObjectId> eventsIds);

    List<CreateCaseEventLog> findAllCreateCaseEventLogsByCaseId(ObjectId caseId);

    List<DeleteCaseEventLog> findAllDeleteCaseEventLogsByCaseId(ObjectId caseId);

    List<GetDataEventLog> findAllGetDataEventLogsByCaseId(ObjectId caseId);

    List<GetDataEventLog> findAllGetDataEventLogsByTaskId(ObjectId taskId);

    List<SetDataEventLog> findAllSetDataEventLogsByCaseId(ObjectId caseId);

    List<SetDataEventLog> findAllSetDataEventLogsByTaskId(ObjectId taskId);

    List<DeletePetriNetEventLog> findAllDeletePetriNetEventLogsByNetId(ObjectId netId);

    List<ImportPetriNetEventLog> findAllImportPetriNetEventLogsByNetId(ObjectId netId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByTaskId(ObjectId taskId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByUserId(String userId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByCaseId(String caseId);

    List<CancelTaskEventLog> findAllCancelTaskEventLogsByTaskId(ObjectId taskId);

    List<CancelTaskEventLog> findAllCancelTaskEventLogsByUserId(String userId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByTaskId(ObjectId taskId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByUserId(String userId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByCaseId(String caseId);
}
