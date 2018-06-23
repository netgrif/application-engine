package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
public class CaseSearchService extends MongoSearchService<Case> {

    private static final Logger log = Logger.getLogger(CaseSearchService.class.getName());

    @Autowired
    private TaskRepository taskRepository;

    // **************************
    // * Query building methods *
    // **************************

    public String authorQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(Long.class, o -> ((Long) o).toString());
        builder.put(Integer.class, o -> ((Integer) o).toString());
        builder.put(ArrayList.class, o -> in((List<Object>) obj, oo -> oo.toString(), ob -> ob instanceof Long || ob instanceof Integer));
        builder.put(String.class, o -> {
            Long id = resolveAuthorByEmail((String) obj);
            return id != null ? id.toString() : "";
        });

        return buildQueryPart("author.id", obj, builder);
    }

    public String titleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in(((List<Object>) obj), ob -> "\"" + ob + "\"", null));
        builder.put(String.class, o -> regex((String) o, "i"));

        return buildQueryPart("title", obj, builder);
    }

    public String petriNetQuery(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> q = (Map<String, Object>) obj;
            if (q.containsKey("id"))
                return petriNetIdQuery(q.get("id"));
            else if (q.containsKey("identifier"))
                return petriNetIdentifierQuery(q.get("identifier"));
        }
        return buildQueryPart("petriNet", null, null);
    }

    public String petriNetIdQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> ref("petriNet", obj));
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> ref("petriNet", oo), null));

        return buildQueryPart("petriNet", obj, builder);
    }

    public String petriNetIdentifierQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in(((List<Object>) obj), ob -> "\"" + ob + "\"", null));
        builder.put(String.class, o -> "\"" + o + "\"");

        return buildQueryPart("processIdentifier", obj, builder);
    }

    public String dataQuery(Object obj) throws IllegalQueryException {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> {
            StringBuilder strBuilder = new StringBuilder();
            ((List<Object>) o).forEach(dataObj -> {
                strBuilder.append(buildDataSetQuery(dataObj));
                strBuilder.append(",");
            });
            strBuilder.deleteCharAt(strBuilder.length() - 1);
            return strBuilder.toString();
        });
        builder.put(LinkedHashSet.class, CaseSearchService::buildDataSetQuery);

        return buildQueryPart(null, obj, builder);
    }

    private static String buildDataSetQuery(Object o) {
        LinkedHashMap<String, Object> dataObj = (LinkedHashMap<String, Object>) o;
        if (!dataObj.containsKey("id") || !dataObj.containsKey("type") || !dataObj.containsKey("value")) return "";
        return "\"dataSet." + dataObj.get("id") + ".value\":" + resolveDataValue(dataObj.get("value"), (String) dataObj.get("type"));
    }

    public String transitionQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> elemMatch(o, oo -> "{\"transition\":\"" + oo + "\"}"));
        builder.put(ArrayList.class, o -> elemMatch(o, oo -> "{\"transition\":" + in(((List<Object>) oo), ob -> "\"" + ob + "\"", null) + "}"));
        builder.put(HashMap.class, o -> {
            Map<String, Object> inner = (Map<String, Object>) o;
            if (inner.get("values") == null)
                return "";
            if (inner.get("combination") != null && ((Boolean) inner.get("combination")))
                return elemMatch(inner.get("values"), oo -> "{\"transition\":" + all(((List<Object>) oo), ob -> "\"" + ob + "\"") + "}");
            else
                return elemMatch(inner.get("values"), oo -> "{\"transition\":" + in(((List<Object>) oo), ob -> "\"" + ob + "\"", null) + "}");
        });

        return buildQueryPart("tasks", obj, builder);
    }
}
