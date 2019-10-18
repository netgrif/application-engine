package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.domain.QFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class FilterSearchService extends MongoSearchService<Filter> {

    public static final String TITLE = "title";
    public static final String VISIBILITY = "visibility";
    public static final String AUTHOR = "author";
    public static final String TYPE = "type";

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
            return authorLong((Long) query);
        return null;
    }

    private Predicate authorLong(Long query) {
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
}
