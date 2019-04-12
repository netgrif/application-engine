package com.netgrif.workflow.workflow.domain.elastic;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.TaskPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.time.LocalDateTime;
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
    private String visualId;

    @Field(type = Keyword)
    private String processIdentifier;

    private String title;

    private LocalDateTime creationDate;

    private Long author;

    private String authorName;

    @Field(type = Keyword)
    private String authorEmail;

    private Map<String, String> dataSet;

    @Field(type = Keyword)
    private Set<String> taskIds;

    @Field(type = Keyword)
    private Set<String> taskMongoIds;

    @Field(type = Keyword)
    private Set<String> enabledRoles;

    public ElasticCase(Case useCase) {
        id = useCase.getStringId();
        visualId = useCase.getVisualId();
        processIdentifier = useCase.getProcessIdentifier();
        title = useCase.getTitle();
        creationDate = useCase.getCreationDate();
        author = useCase.getAuthor().getId();
        authorName = useCase.getAuthor().getFullName();
        authorEmail = useCase.getAuthor().getEmail();
        dataSet = useCase.getDataSet().entrySet().stream().limit(1000).collect(Collectors.toMap(Map.Entry::getKey, f -> "" + f.getValue().getValue()));
        taskIds = useCase.getTasks().stream().map(TaskPair::getTransition).collect(Collectors.toSet());
        taskMongoIds = useCase.getTasks().stream().map(TaskPair::getTask).collect(Collectors.toSet());
        enabledRoles = useCase.getEnabledRoles();
    }
}