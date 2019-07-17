package com.netgrif.workflow.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.TaskPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@SuppressWarnings("OptionalIsPresent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticCaseIndex}", type = "case")
public class ElasticCase {

    @Id
    private String id;

    @Field(type = Keyword)
    private String stringId;

    @Field(type = Keyword)
    private String visualId;

    @Field(type = Keyword)
    private String processIdentifier;

    private String title;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime creationDate;

    private Long creationDateSortable;

    private Long author;

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

    public ElasticCase(Case useCase) {
        stringId = useCase.getStringId();
        processIdentifier = useCase.getProcessIdentifier();
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

        dataSet = new HashMap<>();
        for (String id : useCase.getImmediateDataFields()) {
            Optional<DataField> parseValue = parseValue(useCase.getDataField(id));
            if (parseValue.isPresent()) {
                dataSet.put(id, parseValue.get());
            }
        }
    }

    public void update(ElasticCase useCase) {
        title = useCase.getTitle();
        taskIds = useCase.getTaskIds();
        taskMongoIds = useCase.getTaskMongoIds();
        enabledRoles = useCase.getEnabledRoles();
        dataSet = useCase.getDataSet();
    }

    private Optional<DataField> parseValue(com.netgrif.workflow.workflow.domain.DataField dataField) {
        // Set<I18nString>
        if (dataField.getValue() instanceof User) {
            User user = (User) dataField.getValue();
            if (user == null)
                return Optional.of(new DataField(""," "));
            StringBuilder fullname = new StringBuilder("");
            if (user.getSurname() != null) {
                fullname.append(user.getSurname());
                fullname.append(" ");
            }
            if (user.getName() != null) {
                fullname.append(user.getName());
            }
            return Optional.of(new DataField(String.valueOf(user.getId()), fullname.toString()));
        } else if (dataField.getValue() instanceof LocalDate) {
            LocalDate date = (LocalDate) dataField.getValue();
            if (date == null)
                return Optional.empty();
            return Optional.of(new DataField(String.valueOf(date), date.format(DateTimeFormatter.BASIC_ISO_DATE)));
        } else if (dataField.getValue() instanceof LocalDateTime) {
            LocalDateTime date = (LocalDateTime) dataField.getValue();
            if (date == null)
                return Optional.empty();
            return Optional.of(new DataField(String.valueOf(date), date.format(DateTimeFormatter.BASIC_ISO_DATE)));
        } else {
            if (dataField.getValue() == null)
                return Optional.empty();
            String string = dataField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return Optional.of(new DataField(string));
        }
    }
}