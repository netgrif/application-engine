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
     *
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
        buildStringQuery(request, query);

        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }

    /**
     * Cases with processIdentifier "id" <br>
     * <pre>
     * {
     *     "petriNet": {
     *         "identifier": "id"
     *     }
     * }</pre><br>
     * <p>
     * Cases with processIdentifiers "1" OR "2" <br>
     * <pre>
     * {
     *     "petriNet": [
     *         {
     *             "identifier": "1"
     *         },
     *         {
     *             "identifier": "2"
     *         }
     *     ]
     * }
     * </pre>
     */
    private void buildPetriNetQuery(ElasticSearchRequest request, LoggedUser user, BoolQueryBuilder query) {
        if (request.getPetriNet() == null || request.getPetriNet().isEmpty()) {
            return;
        }

        BoolQueryBuilder petriNetQuery = QueryBuilders.boolQuery();

        for (ElasticSearchRequest.PetriNet petriNet : request.getPetriNet()) {
            if (petriNet.getIdentifier() != null) {
                petriNetQuery.should(QueryBuilders.termQuery("processIdentifier", petriNet.getIdentifier()));
            }
        }

        query.filter(petriNetQuery);
    }

    /**
     * <pre>
     * {
     *     "author": {
     *         "email": "user@customer.com"
     *     }
     * }
     * </pre><br>
     *
     * Cases with author with id 1 AND email "user@customer.com", OR with id 2
     * <pre>
     * {
     *     "author": [{
     *         "id": 1
     *         "email": "user@customer.com"
     *     }, {
     *         "id": 2
     *     }
     *     ]
     * }
     * </pre><br>
     */
    private void buildAuthorQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getAuthor() == null || request.getAuthor().isEmpty()) {
            return;
        }

        BoolQueryBuilder authorsQuery = QueryBuilders.boolQuery();
        for (ElasticSearchRequest.Author author : request.getAuthor()) {
            BoolQueryBuilder authorQuery = QueryBuilders.boolQuery();
            if (author.getEmail() != null) {
                authorQuery.must(QueryBuilders.termQuery("authorEmail", author.getEmail()));
            }
            if (author.getId() != null) {
                authorQuery.must(QueryBuilders.matchQuery("authorName", author.getId()));
            }
            if (author.getName() != null) {
                authorQuery.must(QueryBuilders.termQuery("author", author.getName()));
            }
            authorsQuery.should(authorQuery);
        }

        query.filter(authorsQuery);
    }

    /**
     * Cases with tasks with import Id "nova_uloha"
     * <pre>
     * {
     *     "task": "nova_uloha"
     * }
     * </pre>
     * <p>
     * Cases with tasks with import Id "nova_uloha" OR "kontrola"
     * <pre>
     * {
     *     "task": [
     *         "nova_uloha",
     *         "kontrola"
     *     ]
     * }
     * </pre>
     */
    private void buildTaskQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getTask() == null || request.getTask().isEmpty()) {
            return;
        }

        BoolQueryBuilder taskQuery = QueryBuilders.boolQuery();
        for (String taskImportId : request.getTask()) {
            taskQuery.should(QueryBuilders.termQuery("taskIds", taskImportId));
        }

        query.filter(taskQuery);
    }

    /**
     * Cases with active role "5cb07b6ff05be15f0b972c36"
     * <pre>
     * {
     *     "role": "5cb07b6ff05be15f0b972c36"
     * }
     * </pre>
     *
     * Cases with active role "5cb07b6ff05be15f0b972c36" OR "5cb07b6ff05be15f0b972c31"
     * <pre>
     * {
     *     "role" [
     *         "5cb07b6ff05be15f0b972c36",
     *         "5cb07b6ff05be15f0b972c31"
     *     ]
     * }
     * </pre>
     */
    private void buildRoleQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getRole() == null || request.getRole().isEmpty()) {
            return;
        }

        BoolQueryBuilder roleQuery = QueryBuilders.boolQuery();
        for (String roleId : request.getRole()) {
            roleQuery.should(QueryBuilders.termQuery("enabledRoles", roleId));
        }

        query.filter(roleQuery);
    }

    /**
     * Cases where "text_field" has value "text" AND "number_field" has value 125.<br>
     * <pre>
     * {
     *     "data": {
     *         "text_field": "text",
     *         "number_field": "125"
     *     }
     * }
     * </pre>
     */
    private void buildDataQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getData() == null || request.getData().isEmpty()) {
            return;
        }

        BoolQueryBuilder dataQuery = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> field : request.getData().entrySet()) {
            dataQuery.must(QueryBuilders.matchQuery("dataSet." + field.getKey(), field.getValue()));
        }

        query.filter(dataQuery);
    }

    /**
     * Full text search on fields defined by {@link #fullTextFields()}.
     */
    private void buildFullTextQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getFullText() == null || request.getFullText().isEmpty()) {
            return;
        }

        QueryBuilder fullTextQuery = QueryBuilders.queryStringQuery("*" + request.getFullText() + "*").fields(fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(ElasticSearchRequest request, BoolQueryBuilder query) {
        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            return;
        }

        query.must(QueryBuilders.queryStringQuery(request.getQuery()));
    }
}