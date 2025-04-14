package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public interface IElasticTaskSearchService {

    Page<Task> search(List<ElasticTaskSearchRequest> requests, LoggedIdentity identity, Pageable pageable, Locale locale,
                      Boolean isIntersection, @Nullable BoolQueryBuilder permissionQuery);

    long count(List<ElasticTaskSearchRequest> requests, LoggedIdentity identity, Locale locale, Boolean isIntersection,
               @Nullable BoolQueryBuilder permissionQuery);
}
