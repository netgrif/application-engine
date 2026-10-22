package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public interface IElasticCaseService {

    @Async
    void index(ElasticCase useCase);

    void indexNow(ElasticCase useCase);

    Page<Case> search(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection);

    /**
     * Same as {@link #search(List, LoggedUser, Pageable, Locale, Boolean)}, but Mongo hydration loads only the given
     * {@code dataSet} entries (plus automatically required ones). Returned cases are PARTIAL and READ-ONLY; persist
     * changes with {@code IWorkflowService#savePartial}. A {@code null} {@code dataFieldIds} loads full cases.
     */
    Page<Case> search(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection, Collection<String> dataFieldIds);

    long count(List<CaseSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection);

    void remove(String caseId);
}
