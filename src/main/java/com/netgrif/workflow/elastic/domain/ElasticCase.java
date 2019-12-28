package com.netgrif.workflow.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.elastic.domain.mapping.*;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.TaskPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Nested;

@SuppressWarnings("OptionalIsPresent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticCaseIndex}", type = "case")
public class ElasticCase {

    @Id
    private String id;

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
    private LocalDateTime creationDate;

    private Long creationDateSortable;

    private Long author;

    private String authorName;

    private String authorEmail;

    @Field(type = Nested)
    private Map<String, DataField> dataSet;

    @Field(type = Keyword)
    private Set<String> taskIds;

    @Field(type = Keyword)
    private Set<String> taskMongoIds;

    @Field(type = Keyword)
    private Set<String> enabledRoles;

    public ElasticCase(Case useCase) {
        stringId = useCase.getStringId();
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

        dataSet = new HashMap<>();
        for (String id : useCase.getImmediateDataFields()) {
            Optional<DataField> parseValue = parseValue(useCase.getDataField(id));
            if (parseValue.isPresent()) {
                dataSet.put(id, parseValue.get());
            }
        }
    }

    public void update(ElasticCase useCase) {
        version++;
        lastModified = useCase.getLastModified();
        title = useCase.getTitle();
        taskIds = useCase.getTaskIds();
        taskMongoIds = useCase.getTaskMongoIds();
        enabledRoles = useCase.getEnabledRoles();
        dataSet = useCase.getDataSet();
    }

    private Optional<DataField> parseValue(com.netgrif.workflow.workflow.domain.DataField dataField) {
        // Set<I18nString>
        if (dataField.getValue() instanceof Set) {
            if (dataField.getValue() == null)
                return Optional.empty();
            Set values = (Set) dataField.getValue();
            return Optional.of(new TextField((String) values.stream().map(Object::toString).collect(Collectors.joining(" "))));
        } else if (dataField.getValue() instanceof Number) {
            return Optional.of(new NumberField((Double) dataField.getValue()));
        } else if (dataField.getValue() instanceof User) {
            User user = (User) dataField.getValue();
            if (user == null)
                return Optional.empty();
            StringBuilder fullName = new StringBuilder();
            if (user.getSurname() != null) {
                fullName.append(user.getSurname());
                fullName.append(" ");
            }
            if (user.getName() != null) {
                fullName.append(user.getName());
            }
            return Optional.of(new UserField(user.getId(), user.getEmail(), fullName.toString()));
        } else if (dataField.getValue() instanceof LocalDate) {
            LocalDate date = (LocalDate) dataField.getValue();
            return parseDateField(LocalDateTime.of(date, LocalTime.NOON));
        } else if (dataField.getValue() instanceof LocalDateTime) {
            return parseDateField((LocalDateTime) dataField.getValue());
        } else if (dataField.getValue() instanceof Date) {
            LocalDateTime date = ((Date)dataField.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            return parseDateField(date);
        } else {
            if (dataField.getValue() == null)
                return Optional.empty();
            String string = dataField.getValue().toString();
            if (string == null)
                return Optional.empty();
            return Optional.of(new TextField(string));
        }
    }

    private Optional<DataField> parseDateField(LocalDateTime date) {
        if (date == null)
            return Optional.empty();
        return Optional.of(new DateField(date.format(DateTimeFormatter.BASIC_ISO_DATE), Timestamp.valueOf(date).getTime()));
    }
}