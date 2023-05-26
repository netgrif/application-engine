package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IElasticCaseService {

    @Async
    void index(ElasticCase useCase);

    void indexNow(ElasticCase useCase);

    Page<Case> search(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection);

    long count(List<CaseSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection);

    void remove(String caseId);

    void removeByPetriNetId(String processId);
}