package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.service.query.ElasticQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public abstract class ElasticSearchService {

    protected final ElasticQueryBuilder queryBuilder;

    protected ElasticSearchService(ElasticQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    protected <T> NativeSearchQuery buildQuery(List<T> requests, @Nullable String actorId, Pageable pageable,
                                         Locale locale, Boolean isIntersection, @Nullable BoolQueryBuilder permissionQuery) {
        List<BoolQueryBuilder> singleQueries = requests.stream()
                .map(request -> queryBuilder.buildSingleQuery(request, locale, actorId, permissionQuery))
                .collect(Collectors.toList());

        if (isIntersection && !singleQueries.stream().allMatch(Objects::nonNull)) {
            // one of the queries evaluates to empty set => the entire result is an empty set
            return null;
        } else if (!isIntersection) {
            singleQueries = singleQueries.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (singleQueries.isEmpty()) {
                // all queries result in an empty set => the entire result is an empty set
                return null;
            }
        }

        BinaryOperator<BoolQueryBuilder> reductionOperator = isIntersection ? BoolQueryBuilder::must : BoolQueryBuilder::should;
        BoolQueryBuilder query = singleQueries.stream().reduce(new BoolQueryBuilder(), reductionOperator);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }
}
