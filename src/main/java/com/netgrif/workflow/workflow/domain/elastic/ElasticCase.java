package com.netgrif.workflow.workflow.domain.elastic;

import com.netgrif.workflow.workflow.domain.Case;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "nae", type = "case")
public class ElasticCase {

    @Id
    private String id;

    @Field(type = Keyword)
    private String processIdentifier;

    private String title;

    private Long author;

    private Map<String, String> dataSet;

    @Field(type = Keyword)
    private Set<String> enabledRoles;

    public ElasticCase(Case useCase) {
        id = useCase.getStringId();
        title = useCase.getTitle();
        dataSet = useCase.getDataSet().entrySet().stream().limit(100).collect(Collectors.toMap(Map.Entry::getKey, f -> "" + f.getValue().getValue()));
        processIdentifier = useCase.getProcessIdentifier();
        enabledRoles = useCase.getEnabledRoles();
        author = useCase.getAuthor().getId();
    }
}