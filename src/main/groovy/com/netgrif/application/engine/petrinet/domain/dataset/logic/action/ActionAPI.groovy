package com.netgrif.application.engine.petrinet.domain.dataset.logic.action

import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.petrinet.domain.Process
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.querydsl.core.types.Predicate
import org.bson.types.ObjectId

interface ActionAPI {
    /* TODO: release/8.0.0
    change/make actions - deprecate or remove?
    find user?
    */
    Task findTask(ObjectId id)

    Task findTask(Closure<Predicate> predicate)

    List<Task> findTasks(Closure<Predicate> predicate, int page, int pageSize)

    List<Task> findTasks(String elasticQuery, int page, int pageSize)

    Case findCase(ObjectId id)

    Case findCase(Closure<Predicate> predicate)

    List<Case> findCases(Closure<Predicate> predicate, int page, int pageSize)

    List<Case> findCases(String elasticQuery, int page, int pageSize)

    List<Case> findCases(CaseSearchRequest searchRequest, int page, int pageSize)

    Task assignTask(ObjectId taskId)

    Task assignTask(String transitionId, Case useCase)

    Case createCase(ObjectId netId)

    Case createCase(Process net)

    setData(Field, Map)

    getData()

    findUser()
}