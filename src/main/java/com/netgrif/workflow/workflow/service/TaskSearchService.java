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

    public String titleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));

        return buildQueryPart("title", obj, builder);
    }

    public String userQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(Long.class, o -> ((Long) o).toString());
        builder.put(Integer.class, o -> ((Integer) o).toString());
        builder.put(ArrayList.class, o -> in((List<Object>) obj, oo -> oo.toString(), ob -> ob instanceof Long || ob instanceof Integer));
        builder.put(String.class, o -> {
            Long id = resolveAuthorByEmail((String) obj);
            return id != null ? id.toString() : "";
        });

        return buildQueryPart("userId", obj, builder);
    }

    public String transitionQuery(Object obj){
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));

        return buildQueryPart("transitionId", obj, builder);
    }

    public String processQuery(Object obj){
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));

        return buildQueryPart("processId", obj, builder);
    }


}
