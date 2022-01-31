package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IElasticTaskService {

    Map<String, Float> fullTextFields();

    @Async
    void index(ElasticTask task);

    void indexNow(ElasticTask task);

    Page<Task> search(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection);

    long count(List<ElasticTaskSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection);

    void remove(String taskId);

    void removeByPetriNetId(String petriNetId);
}