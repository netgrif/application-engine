package com.netgrif.application.engine.elastic.service.query;

import com.google.common.collect.ImmutableMap;
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ElasticTaskQueryBuilder implements ElasticQueryBuilder {

    private final Map<String, Float> fullTextFieldMap = ImmutableMap.of(
            "title", 1f,
            "caseTitle", 1f
    );

    private final Map<String, Float> caseTitledMap = ImmutableMap.of(
            "caseTitle", 1f
    );

    /**
     * todo javadoc
     * */
    @Override
    public <T> BoolQueryBuilder buildSingleQuery(T request, Locale locale, @Nullable String actorId,
                                                 @Nullable BoolQueryBuilder permissionQuery) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        ElasticTaskSearchRequest typedRequest = (ElasticTaskSearchRequest) request;

        BoolQueryBuilder query = boolQuery();
        buildCaseQuery(typedRequest, query);
        buildTitleQuery(typedRequest, query);
        buildAssigneeQuery(typedRequest, query);
        buildProcessQuery(typedRequest, query);
        buildFullTextQuery(typedRequest, query);
        buildTransitionQuery(typedRequest, query);
        buildTagsQuery(typedRequest, query);
        buildStringQuery(typedRequest, query, actorId);

        if (permissionQuery != null) {
            query.filter(permissionQuery);
        }

        return query;
    }

    /**
     * See {@link QueryStringQueryBuilder#fields(Map)}
     *
     * @return map where keys are ElasticCase field names and values are boosts of these fields
     */
    public Map<String, Float> fullTextFields() {
        return fullTextFieldMap;
    }

    /**
     * Tasks of case with id "5cb07b6ff05be15f0b972c4d"
     * {
     * "case": {
     * "id": "5cb07b6ff05be15f0b972c4d"
     * }
     * }
     * <p>
     * Tasks of cases with id "5cb07b6ff05be15f0b972c4d" OR "5cb07b6ff05be15f0b972c4e"
     * {
     * "case": [{
     * "id": "5cb07b6ff05be15f0b972c4d"
     * },
     * {
     * "id": "5cb07b6ff05be15f0b972c4e"
     * }]
     * }
     * <p>
     * Tasks of case with case title containing "foo"
     * {
     * "case": {
     * "title": "foo"
     * }
     * }
     * <p>
     * Tasks of case with case title containing "foo" OR "bar"
     * {
     * "case": [{
     * "title": "foo"
     * },
     * {
     * "title: "bar"
     * }]
     * }
     */
    private void buildCaseQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.useCase == null || request.useCase.isEmpty()) {
            return;
        }

        BoolQueryBuilder casesQuery = boolQuery();
        request.useCase.stream().map(this::caseRequestQuery).filter(Objects::nonNull).forEach(casesQuery::should);

        query.filter(casesQuery);
    }

    /**
     * @return query for ID if only ID is present. Query for title if only title is present.
     * If both are present an ID query is returned. If neither are present null is returned.
     */
    private QueryBuilder caseRequestQuery(TaskSearchCaseRequest caseRequest) {
        if (caseRequest.id != null) {
            return termQuery("caseId", caseRequest.id);
        } else if (caseRequest.title != null) {
            return queryStringQuery("*" + caseRequest.title + "*").fields(this.caseTitledMap);
        }
        return null;
    }

    /**
     * Tasks with title (default value) "New task"
     * {
     * "title": "New task"
     * }
     * <p>
     * Tasks with title (default value) "New task" OR "Status"
     * {
     * "title": [
     * "New task",
     * "Status"
     * ]
     * }
     */
    private void buildTitleQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.title == null || request.title.isEmpty()) {
            return;
        }

        BoolQueryBuilder titleQuery = boolQuery();
        for (String title : request.title) {
            titleQuery.should(termQuery("title", title));
        }

        query.filter(titleQuery);
    }

    /**
     * todo javadoc
     * Tasks assigned to user with id 1
     * {
     * "user": 1
     * }
     * <p>
     * Tasks assigned to user with id 1 OR 2
     */
    private void buildAssigneeQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.assigneeId == null || request.assigneeId.isEmpty()) {
            return;
        }

        BoolQueryBuilder assigneeQuery = boolQuery();
        for (String assigneeId : request.assigneeId) {
            assigneeQuery.should(termQuery("assigneeId", assigneeId));
        }

        query.filter(assigneeQuery);
    }

    /**
     * Tasks of process "document"
     * {
     * "process": "document"
     * }
     * <p>
     * Tasks of process "document" OR "folder"
     * {
     * "process": [
     * "document",
     * "folder",
     * ]
     * }
     */
    private void buildProcessQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        BoolQueryBuilder processQuery = boolQuery();
        for (PetriNet process : request.process) {
            if (process.identifier != null) {
                processQuery.should(termQuery("processId", process.identifier));
            }
        }

        query.filter(processQuery);
    }

    /**
     * Full text search on fields defined by {@link #fullTextFields()}.
     */
    private void buildFullTextQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        QueryBuilder fullTextQuery = queryStringQuery("*" + request.fullText + "*").fields(fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * Tasks with transition id "document"
     * {
     * "transitionId": "document"
     * }
     * <p>
     * Tasks with transition id "document" OR "folder"
     * {
     * "transitionId": [
     * "document",
     * "folder",
     * ]
     * }
     */
    private void buildTransitionQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.transitionId == null || request.transitionId.isEmpty()) {
            return;
        }

        BoolQueryBuilder transitionQuery = boolQuery();
        request.transitionId.forEach(transitionId -> transitionQuery.should(termQuery("transitionId", transitionId)));

        query.filter(transitionQuery);
    }

    private void buildTagsQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.tags == null || request.tags.isEmpty()) {
            return;
        }

        BoolQueryBuilder tagsQuery = boolQuery();
        for (Map.Entry<String, String> field : request.tags.entrySet()) {
            tagsQuery.must(termQuery("tags." + field.getKey(), field.getValue()));
        }

        query.filter(tagsQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query, @Nullable String actorId) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        String populatedQuery = request.query;
        if (actorId != null) {
            populatedQuery = request.query.replaceAll(ElasticQueryConstants.ACTOR_ID_TEMPLATE, actorId);
        }

        query.must(queryStringQuery(populatedQuery));
    }
}
