package com.netgrif.application.engine.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticCaseIndex}")
public class ElasticCase {

    @Id
    private String id;

    @Field(type = Keyword)
    private String uriNodeId;

    @Version
    private Long version;

    private Long lastModified;

    @Field(type = Keyword)
    private String stringId;

    private String visualId;

    @Field(type = Keyword)
    private String processIdentifier;

    @Field(type = Keyword)
    private String processId;

    private String title;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime creationDate;

    private Long creationDateSortable;

    @Field(type = Keyword)
    private String author;

    @Field(type = Keyword)
    private String mongoId;

    @Field(type = Keyword)
    private String authorName;

    @Field(type = Keyword)
    private String authorEmail;

    private Map<String, DataField> dataSet;

    @Field(type = Keyword)
    private Set<String> taskIds;

    @Field(type = Keyword)
    private Set<String> taskMongoIds;

    @Field(type = Keyword)
    private Set<String> enabledRoles;

    @Field(type = Keyword)
    private Set<String> viewRoles;

    @Field(type = Keyword)
    private Set<String> viewUserRefs;

    @Field(type = Keyword)
    private Set<String> negativeViewRoles;

    private Set<String> viewUsers;

    @Field(type = Keyword)
    private Set<String> negativeViewUsers;

    /**
     * Data that is stored in the elasticsearch database.
     *
     * Note that the dataSet attribute is NOT set when the object is created and must be set later.
     *
     * The {@link com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService IElasticCaseMappingService} can be used to create
     * instances of this class from Case objects, that have the dataset populated.
     * @param useCase the data object that should be turned into elasticsearch data object
     */
    public ElasticCase(Case useCase) {
        stringId = useCase.getStringId();
        uriNodeId = useCase.getUriNodeId();
        mongoId = useCase.getStringId();   //TODO: Duplication
        lastModified = Timestamp.valueOf(useCase.getLastModified()).getTime();
        processIdentifier = useCase.getProcessIdentifier();
        processId = useCase.getPetriNetId();
        visualId = useCase.getVisualId();
        title = useCase.getTitle();
        creationDate = useCase.getCreationDate();
        creationDateSortable = Timestamp.valueOf(useCase.getCreationDate()).getTime();
        author = useCase.getAuthor().getId();
        authorName = useCase.getAuthor().getFullName();
        authorEmail = useCase.getAuthor().getEmail();
        taskIds = useCase.getTasks().stream().map(TaskPair::getTransition).collect(Collectors.toSet());
        taskMongoIds = useCase.getTasks().stream().map(TaskPair::getTask).collect(Collectors.toSet());
        enabledRoles = new HashSet<>(useCase.getEnabledRoles());
        viewRoles = new HashSet<>(useCase.getViewRoles());
        viewUserRefs = new HashSet<>(useCase.getViewUserRefs());
        negativeViewRoles = new HashSet<>(useCase.getNegativeViewRoles());
        viewUsers = new HashSet<>(useCase.getViewUsers());
        negativeViewUsers = new HashSet<>(useCase.getNegativeViewUsers());

        dataSet = new HashMap<>();
    }

    public void update(ElasticCase useCase) {
        version++;
        lastModified = useCase.getLastModified();
        uriNodeId = useCase.getUriNodeId();
        title = useCase.getTitle();
        taskIds = useCase.getTaskIds();
        taskMongoIds = useCase.getTaskMongoIds();
        enabledRoles = useCase.getEnabledRoles();
        viewRoles = useCase.getViewRoles();
        viewUserRefs = useCase.getViewUserRefs();
        negativeViewRoles = useCase.getNegativeViewRoles();
        viewUsers = useCase.getViewUsers();
        negativeViewUsers = useCase.getNegativeViewUsers();

        dataSet = useCase.getDataSet();
    }
}