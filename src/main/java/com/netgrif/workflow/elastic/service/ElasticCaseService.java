package com.netgrif.workflow.elastic.service;

import com.google.common.collect.ImmutableMap;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository;
import com.netgrif.workflow.elastic.web.ElasticSearchRequest;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Case;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ElasticCaseService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    @Autowired
    private ElasticCaseRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    private Map<String, Float> fullTextFieldMap = ImmutableMap.of(
            "title", 2f,
            "authorName", 1f,
            "authorEmail", 1f
    );

    /**
     * See {@link QueryStringQueryBuilder#fields(Map)}
     * @return map where keys are ElasticCase field names and values are boosts of these fields
     */
    @Override
    public Map<String, Float> fullTextFields() {
        return fullTextFieldMap;
    }


    @Async
    @Override
    public void index(Case useCase) {
        ElasticCase elasticCase = new ElasticCase(useCase);

        repository.save(elasticCase);

        log.info("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" indexed");
    }

    @Override
    public void indexNow(Case useCase) {
        ElasticCase elasticCase = new ElasticCase(useCase);

        repository.save(elasticCase);

        log.info("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" indexed");
    }

    @Override
    public Page<ElasticCase> search(ElasticSearchRequest request, LoggedUser user, Pageable pageable) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        SearchQuery query = buildQuery(request, user, pageable);

        return template.queryForPage(query, ElasticCase.class);
    }

    @Override
    public long count(ElasticSearchRequest request, LoggedUser user) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        SearchQuery query = buildQuery(request, user, new FullPageRequest());

        return template.count(query);
    }

    private SearchQuery buildQuery(ElasticSearchRequest request, LoggedUser user, Pageable pageable) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        buildPetriNetQuery(request, user, query);
        buildAuthorQuery(request, query);
        buildTaskQuery(request, query);
        buildRoleQuery(request, query);
        buildDataQuery(request, query);
        buildFullTextQuery(request, query);

        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }

    private void buildPetriNetQuery(ElasticSearchRequest request, LoggedUser user, BoolQueryBuilder query) {
        // TODO
    }

    private void buildAuthorQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getAuthor() == null || request.getAuthor().isEmpty()) {
            return;
        }

        BoolQueryBuilder authorQuery = QueryBuilders.boolQuery();
        for (ElasticSearchRequest.Author author : request.getAuthor()) {
            if (author.getEmail() != null && author.getEmail().isEmpty()) {
                authorQuery.must(QueryBuilders.termQuery("authorEmail", author.getEmail()));
            }
            if (author.getId() != null) {
                authorQuery.must(QueryBuilders.matchQuery("authorName", author.getId()));
            }
            if (author.getName() != null) {
                authorQuery.must(QueryBuilders.matchQuery("author", author.getName()));
            }
        }

        query.filter(authorQuery);
    }

    private void buildTaskQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        // TODO
    }

    private void buildRoleQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getRole() == null || request.getRole().isEmpty()) {
            return;
        }

        BoolQueryBuilder roleQuery = QueryBuilders.boolQuery();
        for (String roleId : request.getRole()) {
            roleQuery.must(QueryBuilders.termQuery("enabledRoles", roleId));
        }

        query.filter(roleQuery);
    }

    private void buildDataQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        // TODO
    }

    private void buildFullTextQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getFullText() == null || request.getFullText().isEmpty()) {
            return;
        }

        QueryBuilder fullTextQuery = QueryBuilders.queryStringQuery("*"+request.getFullText()+"*").fields(fullTextFields());
        query.must(fullTextQuery);
    }
}