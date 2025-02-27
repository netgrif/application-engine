package com.netgrif.application.engine.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.application.engine.workflow.domain.State;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticTaskIndex}")
public class ElasticTask {

    @Id
    private String id;

    @Field(type = Keyword)
    private String stringId;

    @Field(type = Keyword)
    private String processId;

    @Field(type = Keyword)
    private String caseId;

    @Field(type = Keyword)
    private String taskId;

    @Field(type = Keyword)
    private String transitionId;

    @Field(type = Keyword)
    private State state;

    @Field(type = Keyword)
    private String title; //TODO: i18n

    @Field(type = Keyword)
    private String titleSortable;

    @Field(type = Keyword)
    private String caseColor;

    private String caseTitle;

    @Field(type = Keyword)
    private String caseTitleSortable;

    private int priority;

    private String userId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime startDate;

    @Field(type = Keyword)
    private Set<String> viewProcessRoles;

    @Field(type = Keyword)
    private Set<String> viewCaseRoles;

    @Field(type = Keyword)
    private Set<String> positiveViewProcessRoles;

    @Field(type = Keyword)
    private Set<String> negativeViewProcessRoles;

    @Field(type = Keyword)
    private Set<String> positiveViewCaseRoles;

    @Field(type = Keyword)
    private Set<String> negativeViewCaseRoles;

    @Field(type = Keyword)
    private String icon;

    @Field(type = Keyword)
    private String assignPolicy;

    @Field(type = Keyword)
    private String dataFocusPolicy;

    @Field(type = Keyword)
    private String finishPolicy;

    private Map<String, String> properties;

    public ElasticTask(Task task) {
        this.stringId = task.getStringId();
        this.processId = task.getProcessId();
        this.taskId = task.getStringId();
        this.caseId = task.getCaseId();
        this.transitionId = task.getTransitionId();
        this.title = task.getTitle().getDefaultValue();
        this.titleSortable = title;
        this.state = task.getState();
//        TODO: release/8.0.0
//        this.caseTitle = task.getCaseTitle();
        this.caseTitleSortable = this.caseTitle;
//        if (task.getPriority() != null)
//            this.priority = task.getPriority();
//        this.userId = task.getUserId();
        this.startDate = task.getLastAssigned();
        this.properties = new HashMap<>(task.getProperties());
    }

    public void update(ElasticTask task) {
        this.title = task.getTitle();
        this.titleSortable = title;
        this.state = task.getState();
        this.caseTitle = task.getCaseTitle();
        this.caseTitleSortable = this.caseTitle;
        this.priority = task.getPriority();
        this.userId = task.getUserId();
        this.startDate = task.getStartDate();
        this.viewProcessRoles = task.getViewProcessRoles();
        this.positiveViewProcessRoles = task.getPositiveViewProcessRoles();
        this.viewCaseRoles = task.getViewCaseRoles();
        this.negativeViewProcessRoles = task.getNegativeViewProcessRoles();
        this.positiveViewCaseRoles = task.getPositiveViewCaseRoles();
        this.negativeViewCaseRoles = task.getNegativeViewCaseRoles();
        this.properties = task.getProperties();
    }
}