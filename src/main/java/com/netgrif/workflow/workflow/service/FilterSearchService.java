package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.domain.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class FilterSearchService extends MongoSearchService<Filter> {

    private static final Logger log = LoggerFactory.getLogger(FilterSearchService.class.getName());

    public String titleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in(((List<Object>) obj), ob -> "\"" + ob + "\"", null));
        builder.put(String.class, o -> "\"" + o + "\"");

        return buildQueryPart("title.defaultValue", obj, builder);
    }

    public String visibilityQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(Integer.class, MongoSearchService::lessThenOrEqual);
        builder.put(Long.class, MongoSearchService::lessThenOrEqual);

        return buildQueryPart("visibility", obj, builder);
    }

    public String authorQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(Long.class, Object::toString);
        builder.put(Integer.class, Object::toString);

        if (obj instanceof String)
            return buildQueryPart("author.email", obj, builder);
        return buildQueryPart("author.id", obj, builder);
    }
}
