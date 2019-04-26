package com.netgrif.workflow.elastic.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElasticSearchRequest {

    private List<String> id;

    private List<String> visualId;

    private List<String> processIdentifier;

    private List<String> title;

    private List<Author> author;

    private Map<String, String> data;

    private String fulltext;

    private List<String> task;

    private List<String> role;

    class Author {

        Long id;

        String name;

        String email;
    }
}