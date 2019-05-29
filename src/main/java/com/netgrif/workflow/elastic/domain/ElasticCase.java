package com.netgrif.workflow.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.TaskPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "case", type = "case")
public class ElasticCase {

    @Id
    private String id;

    @Field(type = Keyword)
    private String visualId;

    @Field(type = Keyword)
    private String processIdentifier;

    private String title;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
        taskIds = useCase.getTasks().stream().map(TaskPair::getTransition).collect(Collectors.toSet());
        taskMongoIds = useCase.getTasks().stream().map(TaskPair::getTask).collect(Collectors.toSet());
        enabledRoles = useCase.getEnabledRoles();

        dataSet = new HashMap<>();
        for (String id : useCase.getImmediateDataFields()) {
            dataSet.put(id, String.valueOf(useCase.getFieldValue(id)));
        }
    }

    public void update(ElasticCase useCase) {
        title = useCase.getTitle();
        taskIds = useCase.getTaskIds();
        taskMongoIds = useCase.getTaskMongoIds();
        enabledRoles = useCase.getEnabledRoles();
        dataSet = useCase.getDataSet();
    }
}