package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.elastic.service.query.ElasticCaseQueryBuilder;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
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
public class ElasticCaseSearchService extends ElasticSearchService implements IElasticCaseSearchService {

    private final ElasticCaseRepository repository;
    private final IWorkflowService workflowService;
    private final ElasticsearchRestTemplate template;
    private final ElasticsearchProperties properties;

    public ElasticCaseSearchService(ElasticCaseRepository repository, @Lazy IWorkflowService workflowService,
                                    ElasticsearchRestTemplate template, ElasticsearchProperties properties,
                                    IPetriNetService petriNetService, IElasticCasePrioritySearch elasticCasePrioritySearch) {
        super(new ElasticCaseQueryBuilder(petriNetService, elasticCasePrioritySearch));
        this.repository = repository;
        this.workflowService = workflowService;
        this.template = template;
        this.properties = properties;
    }

    @Override
    public Page<Case> search(List<CaseSearchRequest> requests, @Nullable LoggedIdentity identity, Pageable pageable,
                             Locale locale, Boolean isIntersection, @Nullable BoolQueryBuilder permissionQuery) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        pageable = ((ElasticCaseQueryBuilder) queryBuilder).resolveUnmappedSortAttributes(pageable);
        NativeSearchQuery query = buildQuery(requests, identity, pageable, locale, isIntersection, permissionQuery);
        List<Case> casePage;
        long total;
        if (query != null) {
            SearchHits<ElasticCase> hits = template.search(query, ElasticCase.class,
                    IndexCoordinates.of(properties.getIndex().get("case")));
            Page<ElasticCase> indexedCases = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits,
                    query.getPageable()));
            casePage = workflowService.findAllById(indexedCases.get().map(ElasticCase::getStringId).collect(Collectors.toList()));
            total = indexedCases.getTotalElements();
        } else {
            casePage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(casePage, pageable, total);
    }

    @Override
    public long count(List<CaseSearchRequest> requests, @Nullable LoggedIdentity identity, Locale locale,
                      Boolean isIntersection, @Nullable BoolQueryBuilder permissionQuery) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        NativeSearchQuery query = buildQuery(requests, identity, new FullPageRequest(), locale, isIntersection, permissionQuery);

        return query != null ? template.count(query, ElasticCase.class) : 0;
    }

    @Override
    public String findUriNodeId(Case aCase) {
        if (aCase == null) {
            return null;
        }
        ElasticCase elasticCase = repository.findByStringId(aCase.getStringId());
        if (elasticCase == null) {
            log.warn("[{}] Case with id [{}] is not indexed.", aCase.getStringId(), aCase.getStringId());
            return null;
        }

        return elasticCase.getUriNodeId();
    }
}