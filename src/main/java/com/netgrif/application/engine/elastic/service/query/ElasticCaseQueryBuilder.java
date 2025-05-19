package com.netgrif.application.engine.elastic.service.query;

import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticCaseQueryBuilder implements ElasticQueryBuilder {

    private final IElasticCasePrioritySearch elasticCasePrioritySearch;

    /**
     * todo javadoc
     * */
    @Override
    public <T> BoolQueryBuilder buildSingleQuery(T request, Locale locale, @Nullable String actorId,
                                                 @Nullable BoolQueryBuilder permissionQuery) {
        CaseSearchRequest typedRequest = (CaseSearchRequest) request;
        BoolQueryBuilder query = boolQuery();

        buildPetriNetQuery(typedRequest, query);
        buildAuthorQuery(typedRequest, query);
        buildTaskQuery(typedRequest, query);
        buildRoleQuery(typedRequest, query);
        buildDataQuery(typedRequest, query);
        buildFullTextQuery(typedRequest, query);
        buildStringQuery(typedRequest, query, actorId);
        buildCaseIdQuery(typedRequest, query);
        buildUriNodeIdQuery(typedRequest, query);
        buildTagsQuery(typedRequest, query);

        if (permissionQuery != null) {
            query.filter(permissionQuery);
        }

        return query;
    }

    public Pageable resolveUnmappedSortAttributes(Pageable pageable) {
        if (pageable.isUnpaged()) {
            log.warn("Provided pageable is unpaged. Skipping sorting...");
            return pageable;
        }

        List<Sort.Order> modifiedOrders = new ArrayList<>();
        pageable.getSort().iterator().forEachRemaining(order -> modifiedOrders.add(new Order(order.getDirection(),
                order.getProperty()).withUnmappedType("keyword")));

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()).withSort(Sort.by(modifiedOrders));
    }

    private void buildPetriNetQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        BoolQueryBuilder petriNetQuery = boolQuery();

        for (CaseSearchRequest.PetriNet process : request.process) {
            if (process.identifier != null) {
                petriNetQuery.should(termQuery("processIdentifier", process.identifier));
            }
            if (process.processId != null) {
                petriNetQuery.should(termQuery("processId", process.processId));
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
     * <p>
     * Cases with author with (id 1 AND email "user@customer.com") OR (id 2)
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
    private void buildAuthorQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.author == null || request.author.isEmpty()) {
            return;
        }

        BoolQueryBuilder authorsQuery = boolQuery();
        for (CaseSearchRequest.Author author : request.author) {
            BoolQueryBuilder authorQuery = boolQuery();
            if (author.email != null) {
                authorQuery.must(termQuery("authorEmail", author.email));
            }
            if (author.id != null) {
                authorQuery.must(matchQuery("authorId", author.id));
            }
            if (author.name != null) {
                authorQuery.must(termQuery("authorName", author.name));
            }
            authorsQuery.should(authorQuery);
        }

        query.filter(authorsQuery);
    }

    /**
     * Cases with tasks with import Id "nova_uloha"
     * <pre>
     * {
     *     "transition": "nova_uloha"
     * }
     * </pre>
     * <p>
     * Cases with tasks with import Id "nova_uloha" OR "kontrola"
     * <pre>
     * {
     *     "transition": [
     *         "nova_uloha",
     *         "kontrola"
     *     ]
     * }
     * </pre>
     */
    private void buildTaskQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.transition == null || request.transition.isEmpty()) {
            return;
        }

        BoolQueryBuilder taskQuery = boolQuery();
        for (String taskImportId : request.transition) {
            taskQuery.should(termQuery("taskIds", taskImportId));
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
     * <p>
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
    private void buildRoleQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.role == null || request.role.isEmpty()) {
            return;
        }

        BoolQueryBuilder roleQuery = boolQuery();
        for (String roleId : request.role) {
            roleQuery.should(termQuery("enabledRoles", roleId));
        }

        query.filter(roleQuery);
    }

    /**
     * Cases where "text_field" has value EXACTLY "text" AND "number_field" has value EXACTLY "125".<br>
     * <pre>
     * {
     *     "data": {
     *         "text_field": "text",
     *         "number_field": "125"
     *     }
     * }
     * </pre>
     */
    private void buildDataQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.data == null || request.data.isEmpty()) {
            return;
        }

        BoolQueryBuilder dataQuery = boolQuery();
        for (Map.Entry<String, String> field : request.data.entrySet()) {
            if (field.getKey().contains("."))
                dataQuery.must(termQuery("dataSet." + field.getKey(), field.getValue()));
            else
                dataQuery.must(termQuery("dataSet." + field.getKey() + ".fulltextValue.keyword", field.getValue()));
        }

        query.filter(dataQuery);
    }

    private void buildTagsQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.tags == null || request.tags.isEmpty()) {
            return;
        }

        BoolQueryBuilder tagsQuery = boolQuery();
        for (Map.Entry<String, String> field : request.tags.entrySet()) {
            tagsQuery.must(termQuery("tags." + field.getKey(), field.getValue()));
        }

        query.filter(tagsQuery);
    }

    private void buildFullTextQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        // TODO: improvement? wildcard does not scale good
        //String searchText = elasticsearchProperties.isAnalyzerEnabled() ? request.fullText : "*" + request.fullText + "*";
        String searchText = "*" + request.fullText + "*";
        QueryBuilder fullTextQuery = queryStringQuery(searchText).fields(elasticCasePrioritySearch.fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(CaseSearchRequest request, BoolQueryBuilder query, @Nullable String actorId) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        String populatedQuery = request.query;
        if (actorId != null) {
            populatedQuery = populatedQuery.replaceAll(ElasticQueryConstants.ACTOR_ID_TEMPLATE, actorId);
        }

        query.must(queryStringQuery(populatedQuery).allowLeadingWildcard(true).analyzeWildcard(true));
    }

    /**
     * Case with stringId "5cb07b6ff05be15f0b972c36"
     * <pre>
     * {
     *     "stringId": "5cb07b6ff05be15f0b972c36"
     * }
     * </pre>
     * <p>
     * Cases with stringId "5cb07b6ff05be15f0b972c36" OR "5cb07b6ff05be15f0b972c31"
     * <pre>
     * {
     *     "stringId" [
     *         "5cb07b6ff05be15f0b972c36",
     *         "5cb07b6ff05be15f0b972c31"
     *     ]
     * }
     * </pre>
     */
    private void buildCaseIdQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.stringId == null || request.stringId.isEmpty()) {
            return;
        }

        BoolQueryBuilder caseIdQuery = boolQuery();
        request.stringId.forEach(caseId -> caseIdQuery.should(termQuery("stringId", caseId)));
        query.filter(caseIdQuery);
    }

    private void buildUriNodeIdQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.uriNodeId == null || request.uriNodeId.isEmpty()) {
            return;
        }

        BoolQueryBuilder caseIdQuery = boolQuery();
        caseIdQuery.should(termQuery("uriNodeId", request.uriNodeId));
        query.filter(caseIdQuery);
    }
}
