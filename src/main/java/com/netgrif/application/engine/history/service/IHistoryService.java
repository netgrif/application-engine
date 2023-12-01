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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public interface IHistoryService {

    void save(EventLog eventLog);

    List<EventLog> findAllByIds(List<ObjectId> eventsIds);

    List<CreateCaseEventLog> findAllCreateCaseEventLogsByCaseId(ObjectId caseId);

    List<DeleteCaseEventLog> findAllDeleteCaseEventLogsByCaseId(ObjectId caseId);

    List<CreateCaseEventLog> findAllCreateCaseEventLogsByCaseId(String caseId);

    List<DeleteCaseEventLog> findAllDeleteCaseEventLogsByCaseId(String caseId);

    List<GetDataEventLog> findAllGetDataEventLogsByCaseId(ObjectId caseId);

    List<GetDataEventLog> findAllGetDataEventLogsByTaskId(ObjectId taskId);

    List<GetDataEventLog> findAllGetDataEventLogsByCaseId(String caseId);

    List<GetDataEventLog> findAllGetDataEventLogsByTaskId(String taskId);

    List<SetDataEventLog> findAllSetDataEventLogsByCaseId(ObjectId caseId);

    List<SetDataEventLog> findAllSetDataEventLogsByCaseId(String caseId);

    List<SetDataEventLog> findAllSetDataEventLogsByTaskId(ObjectId taskId);

    List<DeletePetriNetEventLog> findAllDeletePetriNetEventLogsByNetId(ObjectId netId);

    List<SetDataEventLog> findAllSetDataEventLogsByTaskId(String taskId);

    List<ImportPetriNetEventLog> findAllImportPetriNetEventLogsByNetId(ObjectId netId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByTaskId(ObjectId taskId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByTaskId(String taskId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByUserId(String userId);

    List<AssignTaskEventLog> findAllAssignTaskEventLogsByCaseId(String caseId);

    List<CancelTaskEventLog> findAllCancelTaskEventLogsByTaskId(String taskId);

    List<CancelTaskEventLog> findAllCancelTaskEventLogsByTaskId(ObjectId taskId);

    List<CancelTaskEventLog> findAllCancelTaskEventLogsByUserId(String userId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByTaskId(String taskId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByTaskId(ObjectId taskId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByUserId(String userId);

    List<FinishTaskEventLog> findAllFinishTaskEventLogsByCaseId(String caseId);

    <T> Page<T> findByUserId(String id, Class<T> clazz, Pageable pageable);

    <T> List<T> findAllByUserId(String id, Class<T> clazz);

    <T> Page<T> findByNetId(String id, Class<T> clazz, Pageable pageable);

    <T> List<T> findAllByNetId(String id, Class<T> clazz);

    <T> Page<T> findByNetId(ObjectId id, Class<T> clazz, Pageable pageable);

    <T> List<T> findAllByNetId(ObjectId id, Class<T> clazz);

    <T> Page<T> findByTaskId(String id, Class<T> clazz);

    <T> Page<T> findByTaskId(String id, Class<T> clazz, Pageable pageable);

    <T> List<T> findAllByTaskId(String id, Class<T> clazz);

    <T> Page<T> findByCaseId(String id, Class<T> clazz, Pageable pageable);

    <T> List<T> findAllByCaseId(String id, Class<T> clazz);

    <T> Page<T> findByQuery(Query query, Class<T> clazz, Pageable pageable);

    <T> Page<T> findByQuery(String queryString, Class<T> clazz, Pageable pageable);

    <T> List<T> findAllByQuery(Query query, Class<T> clazz);
}
