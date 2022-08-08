package com.netgrif.application.engine.elastic.web.requestbodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseSearchRequest {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<PetriNet> process;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> processIdentifier;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<Author> author;

    public Map<String, String> data;

    public String fullText;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> transition;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> role;

    public String query;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> stringId;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<String> group;

    public CaseSearchRequest(Map<String, Object> request) {
        if (request.containsKey("process") && request.get("process") instanceof List) {
            List<String> processIdentifiers = (List<String>) request.get("process");
            this.process = processIdentifiers.stream().map(PetriNet::new).collect(Collectors.toList());
        }
        if (request.containsKey("processIdentifier") && request.get("processIdentifier") instanceof List) {
            this.processIdentifier = (List<String>) request.get("processIdentifier");
        }
        if (request.containsKey("author") && request.get("author") instanceof List) {
            List<Map<String, String>> authors = (List<Map<String, String>>) request.get("author");
            this.author = authors.stream().map(map ->  {
                Author authorRequest = new Author();
                if (map.containsKey("id"))
                    authorRequest.id = map.get("id");
                if (map.containsKey("name"))
                    authorRequest.name = map.get("name");
                if (map.containsKey("email"))
                    authorRequest.email = map.get("email");
                return authorRequest;
            }).collect(Collectors.toList());
        }
        if (request.containsKey("data") && request.get("data") instanceof Map) {
            this.data = (Map<String, String>) request.get("data");
        }
        if (request.containsKey("fullText") && request.get("fullText") instanceof String) {
            this.fullText = (String) request.get("fullText");
        }
        if (request.containsKey("transition") && request.get("transition") instanceof List) {
            this.transition = (List<String>) request.get("transition");
        }
        if (request.containsKey("role") && request.get("role") instanceof List) {
            this.role = (List<String>) request.get("role");
        }
        if (request.containsKey("query") && request.get("query") instanceof String) {
            this.query = (String) request.get("query");
        }
        if (request.containsKey("stringId") && request.get("stringId") instanceof List) {
            this.stringId = (List<String>) request.get("stringId");
        }
        if (request.containsKey("group") && request.get("group") instanceof List) {
            this.group = (List<String>) request.get("group");
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetriNet {

        public String identifier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {

        public String id;

        public String name;

        public String email;
    }
}