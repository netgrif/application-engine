package com.netgrif.application.engine.elastic.web.requestbodies;

import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
public class ElasticTaskSearchRequest extends TaskSearchRequest {
    public String query;
    
    public ElasticTaskSearchRequest(Map<String, Object> request) {
        if (request.containsKey("role") && request.get("role") instanceof List) {
            this.role = (List<String>) request.get("role");
        }
        if (request.containsKey("useCase") && request.get("useCase") instanceof List) {
            List<Map<String, String>> useCases = (List<Map<String, String>>) request.get("useCase");
            this.useCase = useCases.stream().map(map ->  {
                TaskSearchCaseRequest useCase = new TaskSearchCaseRequest();
                if (map.containsKey("id"))
                    useCase.id = map.get("id");
                if (map.containsKey("title"))
                    useCase.title = map.get("title");
                return useCase;
            }).collect(Collectors.toList());
        }
        if (request.containsKey("title") && request.get("title") instanceof List) {
            this.title = (List<String>) request.get("title");
        }
        if (request.containsKey("user") && request.get("user") instanceof List) {
            this.user = (List<String>) request.get("user");
        }
        if (request.containsKey("process") && request.get("process") instanceof List) {
            List<String> processIdentifiers = (List<String>) request.get("process");
            this.process = processIdentifiers.stream().map(PetriNet::new).collect(Collectors.toList());
        }
        if (request.containsKey("transitionId") && request.get("transitionId") instanceof List) {
            this.transitionId = (List<String>) request.get("transitionId");
        }
        if (request.containsKey("fullText") && request.get("fullText") instanceof String) {
            this.fullText = (String) request.get("fullText");
        }
        if (request.containsKey("group") && request.get("group") instanceof List) {
            this.group = (List<String>) request.get("group");
        }
        if (request.containsKey("users") && request.get("users") instanceof List) {
            this.users = (List<String>) request.get("users");
        }
        if (request.containsKey("query") && request.get("query") instanceof String) {
            this.query = (String) request.get("query");
        }
    }
}
