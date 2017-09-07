package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class TaskSearchService extends MongoSearchService<Task> {

    public String roleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"roles." + obj + "\":" + exists(true));
        builder.put(ArrayList.class, o -> {
            StringBuilder expression = new StringBuilder();
            ((List) o).forEach(role -> {
                expression.append("{\"roles.");
                expression.append(role);
                expression.append("\":");
                expression.append(exists(true));
                expression.append("},");
            });
            return expression.substring(1, expression.length() - 2);
        });

        return buildQueryPart(null, obj, builder);
    }

    public String caseQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");

        return buildQueryPart("caseId", obj, builder);
    }


}
