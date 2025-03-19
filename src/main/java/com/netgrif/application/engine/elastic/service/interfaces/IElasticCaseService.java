package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Locale;

public interface IElasticCaseService {

    @Async
    void index(Case useCase);

    void indexNow(Case useCase);

    Page<Case> search(List<CaseSearchRequest> requests, LoggedIdentity identity, Pageable pageable, Locale locale, Boolean isIntersection);

    long count(List<CaseSearchRequest> requests, LoggedIdentity identity, Locale locale, Boolean isIntersection);

    void remove(String caseId);

    void removeByPetriNetId(String processId);

    String findUriNodeId(Case aCase);
}