package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.domain.*;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskSearchService;
import com.netgrif.application.engine.elastic.service.query.ElasticTaskQueryBuilder;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * todo javadoc
 * */
@Slf4j
@Service
public class ElasticTaskSearchService extends ElasticSearchService implements IElasticTaskSearchService {

    private final ITaskService taskService;
    private final ElasticsearchRestTemplate template;
    private final ElasticsearchProperties properties;
    private final ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    public ElasticTaskSearchService(@Lazy ITaskService taskService, ElasticsearchRestTemplate template,
                                    ElasticsearchProperties properties, ElasticsearchRestTemplate elasticsearchTemplate) {
        super(new ElasticTaskQueryBuilder());
        this.taskService = taskService;
        this.template = template;
        this.properties = properties;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public Page<Task> search(List<ElasticTaskSearchRequest> requests, @Nullable LoggedIdentity identity, Pageable pageable,
                             Locale locale, Boolean isIntersection, @Nullable BoolQueryBuilder permissionQuery) {
        NativeSearchQuery query = buildQuery(requests, identity, pageable, locale, isIntersection, permissionQuery);
        List<Task> taskPage;
        long total;
        if (query != null) {
            SearchHits<ElasticTask> hits = elasticsearchTemplate.search(query, ElasticTask.class, IndexCoordinates.of(properties.getIndex().get("task")));
            Page<ElasticTask> indexedTasks = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
            taskPage = taskService.findAllById(indexedTasks.get().map(ElasticTask::getStringId).collect(Collectors.toList()));
            total = indexedTasks.getTotalElements();
        } else {
            taskPage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(taskPage, pageable, total);
    }

    @Override
    public long count(List<ElasticTaskSearchRequest> requests, @Nullable LoggedIdentity identity, Locale locale,
                      Boolean isIntersection, @Nullable BoolQueryBuilder permissionQuery) {
        NativeSearchQuery query = buildQuery(requests, identity, new FullPageRequest(), locale, isIntersection, permissionQuery);

        return query != null ? template.count(query, ElasticTask.class) : 0;
    }
}