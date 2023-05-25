package com.netgrif.application.engine.history.service;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import com.netgrif.application.engine.history.domain.baseevent.repository.EventLogRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService implements IHistoryService {
    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    @Async
    public void save(EventLog eventLog) {
        eventLogRepository.save(eventLog);
    }

    protected String clazz = "', _class: '";
    
    @Override
    public List<EventLog> findAllByIds(List<ObjectId> eventIds) {
        return eventLogRepository.findAllById(eventIds);
    }

    @Override
    public List<CreateCaseEventLog> findAllCreateCaseEventLogsByCaseId(ObjectId caseId) {
        return findAllByCaseId(caseId.toString(), CreateCaseEventLog.class);
    }

    @Override
    public List<DeleteCaseEventLog> findAllDeleteCaseEventLogsByCaseId(ObjectId caseId) {
        return findAllByCaseId(caseId.toString(), DeleteCaseEventLog.class);
    }

    @Override
    public List<CreateCaseEventLog> findAllCreateCaseEventLogsByCaseId(String caseId) {
        return findAllByCaseId(caseId, CreateCaseEventLog.class);
    }

    @Override
    public List<DeleteCaseEventLog> findAllDeleteCaseEventLogsByCaseId(String caseId) {
        return findAllByCaseId(caseId, DeleteCaseEventLog.class);
    }

    @Override
    public List<GetDataEventLog> findAllGetDataEventLogsByCaseId(ObjectId caseId) {
        return findAllByCaseId(caseId.toString(), GetDataEventLog.class);
    }

    @Override
    public List<GetDataEventLog> findAllGetDataEventLogsByTaskId(ObjectId taskId) {
        return findAllByTaskId(taskId.toString(), GetDataEventLog.class);
    }

    @Override
    public List<GetDataEventLog> findAllGetDataEventLogsByCaseId(String caseId) {
        return findAllByCaseId(caseId, GetDataEventLog.class);
    }

    @Override
    public List<GetDataEventLog> findAllGetDataEventLogsByTaskId(String taskId) {
        return findAllByTaskId(taskId, GetDataEventLog.class);
    }

    @Override
    public List<SetDataEventLog> findAllSetDataEventLogsByCaseId(ObjectId caseId) {
        return findAllByCaseId(caseId.toString(), SetDataEventLog.class);
    }

    @Override
    public List<SetDataEventLog> findAllSetDataEventLogsByCaseId(String caseId) {
        return findAllByCaseId(caseId, SetDataEventLog.class);
    }

    @Override
    public List<SetDataEventLog> findAllSetDataEventLogsByTaskId(ObjectId taskId) {
        return findAllByTaskId(taskId.toString(), SetDataEventLog.class);
    }

    @Override
    public List<DeletePetriNetEventLog> findAllDeletePetriNetEventLogsByNetId(ObjectId netId) {
        return findAllByNetId(netId, DeletePetriNetEventLog.class);
    }

    @Override
    public List<SetDataEventLog> findAllSetDataEventLogsByTaskId(String taskId) {
        return findAllByTaskId(taskId, SetDataEventLog.class);
    }

    @Override
    public List<ImportPetriNetEventLog> findAllImportPetriNetEventLogsByNetId(ObjectId netId) {
        return findAllByNetId(netId, ImportPetriNetEventLog.class);
    }

    @Override
    public List<AssignTaskEventLog> findAllAssignTaskEventLogsByTaskId(ObjectId taskId) {
        return findAllByTaskId(taskId.toString(), AssignTaskEventLog.class);
    }

    @Override
    public List<AssignTaskEventLog> findAllAssignTaskEventLogsByTaskId(String taskId) {
        return findAllByTaskId(taskId, AssignTaskEventLog.class);
    }


    @Override
    public List<AssignTaskEventLog> findAllAssignTaskEventLogsByUserId(String userId) {
        return findAllByUserId(userId, AssignTaskEventLog.class);
    }

    @Override
    public List<AssignTaskEventLog> findAllAssignTaskEventLogsByCaseId(String caseId) {
        return findAllByCaseId(caseId, AssignTaskEventLog.class);
    }

    @Override
    public List<CancelTaskEventLog> findAllCancelTaskEventLogsByTaskId(String taskId) {
        return findAllByTaskId(taskId, CancelTaskEventLog.class);
    }

    @Override
    public List<CancelTaskEventLog> findAllCancelTaskEventLogsByTaskId(ObjectId taskId) {
        return findAllByTaskId(taskId.toString(), CancelTaskEventLog.class);
    }

    @Override
    public List<CancelTaskEventLog> findAllCancelTaskEventLogsByUserId(String userId) {
        return findAllByUserId(userId, CancelTaskEventLog.class);
    }


    @Override
    public List<FinishTaskEventLog> findAllFinishTaskEventLogsByTaskId(String taskId) {
        return findAllByTaskId(taskId, FinishTaskEventLog.class);
    }

    @Override
    public List<FinishTaskEventLog> findAllFinishTaskEventLogsByTaskId(ObjectId taskId) {
        return findAllByTaskId(taskId.toString(), FinishTaskEventLog.class);
    }

    @Override
    public List<FinishTaskEventLog> findAllFinishTaskEventLogsByUserId(String userId) {
        return findAllByUserId(userId, FinishTaskEventLog.class);
    }

    @Override
    public List<FinishTaskEventLog> findAllFinishTaskEventLogsByCaseId(String caseId) {
        return findAllByCaseId(caseId, FinishTaskEventLog.class);
    }

    @Override
    public <T> Page<T> findByUserId(String id, Class<T> clazz, Pageable pageable) {
        String queryString = "{userId: '" + id + this.clazz + clazz.getName() + "'}";
        return findByQuery(queryString, clazz, pageable);
    }

    @Override
    public <T> List<T> findAllByUserId(String id, Class<T> clazz) {
        String queryString = "{userId: '" + id + this.clazz + clazz.getName() + "'}";
        Query query = new BasicQuery(queryString);
        return findAllByQuery(query, clazz);
    }

    @Override
    public <T> Page<T> findByNetId(String id, Class<T> clazz, Pageable pageable) {
        return findByNetId(new ObjectId(id), clazz, pageable);
    }

    @Override
    public <T> List<T> findAllByNetId(String id, Class<T> clazz) {
        return findAllByNetId(new ObjectId(id), clazz);
    }

    @Override
    public <T> Page<T> findByNetId(ObjectId id, Class<T> clazz, Pageable pageable) {
        String queryString = "{netId: '" + id + this.clazz + clazz.getName() + "'}";
        return findByQuery(queryString, clazz, pageable);
    }

    @Override
    public <T> List<T> findAllByNetId(ObjectId id, Class<T> clazz) {
        String queryString = "{netId: '" + id + this.clazz + clazz.getName() + "'}";
        Query query = new BasicQuery(queryString);
        return findAllByQuery(query, clazz);
    }

    @Override
    public <T> Page<T> findByTaskId(String id, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> Page<T> findByTaskId(String id, Class<T> clazz, Pageable pageable) {
        String queryString = "{taskId: '" + id + this.clazz + clazz.getName() + "'}";
        return findByQuery(queryString, clazz, pageable);
    }

    @Override
    public <T> List<T> findAllByTaskId(String id, Class<T> clazz) {
        String queryString = "{taskId: '" + id + this.clazz + clazz.getName() + "'}";
        Query query = new BasicQuery(queryString);
        return findAllByQuery(query, clazz);
    }

    @Override
    public <T> Page<T> findByCaseId(String id, Class<T> clazz, Pageable pageable) {
        String queryString = "{caseId: '" + id + this.clazz + clazz.getName() + "'}";
        return findByQuery(queryString, clazz, pageable);
    }

    @Override
    public <T> List<T> findAllByCaseId(String id, Class<T> clazz) {
        String queryString = "{caseId: '" + id + this.clazz + clazz.getName() + "'}";
        Query query = new BasicQuery(queryString);
        return findAllByQuery(query, clazz);
    }

    @Override
    public <T> Page<T> findByQuery(Query query, Class<T> clazz, Pageable pageable) {
        return null;
    }

    @Override
    public <T> Page<T> findByQuery(String queryString, Class<T> clazz, Pageable pageable) {
        Query query = new BasicQuery(queryString).with(pageable);
        return new PageImpl<>(mongoTemplate.find(query, clazz),
                pageable,
                mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), clazz));
    }

    @Override
    public <T> List<T> findAllByQuery(Query query, Class<T> clazz) {
        return mongoTemplate.find(query, clazz);
    }

}
