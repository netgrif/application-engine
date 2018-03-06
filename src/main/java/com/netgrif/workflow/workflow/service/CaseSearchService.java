package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        builder.put(String.class, o -> "\"" + o + "\"");

        return buildQueryPart("title", obj, builder);
    }

    public String petriNetQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> ref("petriNet", obj));
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> ref("petriNet", oo), null));

        return buildQueryPart("petriNet", obj, builder);
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

        builder.put(String.class, o -> "\"" + o + "\"");
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> "\"" + oo + "\"", null));
        builder.put(HashMap.class, o -> {
            return "";
        });

//        builder.put(String.class, o -> {
//            List<Task> tasks = taskRepository.findAllByTransitionIdIn(Collections.singletonList((String) o));
//            return in(tasks.stream().map(Task::getCaseId).collect(Collectors.toList()), ob -> oid((String) ob), null);
//        });
//        builder.put(ArrayList.class, o -> {
//            List<Task> tasks = taskRepository.findAllByTransitionIdIn((List<String>) o);
//            return in(new ArrayList<>(tasks.stream().map(Task::getCaseId).collect(Collectors.toSet())), ob -> oid((String) ob), null);
//        });

        return buildQueryPart("tasks", obj, builder);
    }
}
