package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.workflow.domain.Filter;
import com.netgrif.application.engine.workflow.domain.QFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Service
public class FilterSearchService extends MongoSearchService<Filter> {

    public static final String TITLE = "title";
    public static final String VISIBILITY = "visibility";
    public static final String AUTHOR = "author";
    public static final String TYPE = "type";
    public static final String FILTER_ID = "id";

    public Predicate buildQuery(Map<String, Object> request, LoggedUser user, Locale locale) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.containsKey(TITLE))
            builder.and(title(request.get(TITLE)));
        if (request.containsKey(VISIBILITY))
            builder.and(visibility(request.get(VISIBILITY)));
        if (request.containsKey(AUTHOR))
            builder.and(author(request.get(AUTHOR)));
        if (request.containsKey(TYPE))
            builder.and(type(request.get(TYPE)));
        if (request.containsKey(FILTER_ID))
            builder.and(id(request.get(FILTER_ID)));

        return builder;
    }

    public Predicate title(Object query) {
        if (query instanceof String)
            return titleString((String) query);
        return null;
    }

    private Predicate titleString(String query) {
        return QFilter.filter.title.defaultValue.containsIgnoreCase(query);
    }

    public Predicate visibility(Object query) {
        if (query instanceof Integer)
            return visibilityInteger((Integer) query);
        return null;
    }

    private Predicate visibilityInteger(Integer query) {
        return QFilter.filter.visibility.eq(query);
    }

    public Predicate author(Object query) {
        if (query instanceof Long)
            return authorLong((String) query);
        return null;
    }

    private Predicate authorLong(String query) {
        return QFilter.filter.author.id.eq(query);
    }

    public Predicate type(Object query) {
        if (query instanceof String)
            return typeString((String) query);
        return null;
    }

    private Predicate typeString(String query) {
        return QFilter.filter.type.eq(query);
    }

    public Predicate id(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).stream().filter(q -> q instanceof String).map(q -> idString((String) q)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof String)
            return idString((String) query);
        return null;
    }

    private Predicate idString(String query) {
        return QFilter.filter._id.eq(new ObjectId(query));
    }
}
