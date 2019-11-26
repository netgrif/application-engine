package com.netgrif.workflow.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.workflow.workflow.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticTaskIndex}", type = "task")
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
    private String transitionId;

    private String title; //TODO: i18n

    @Field(type = Keyword)
    private String titleSortable;

    @Field(type = Keyword)
    private String caseColor;

    private String caseTitle;

    @Field(type = Keyword)
    private String caseTitleSortable;

    private int priority;

    private Long userId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @Field(type = Keyword)
    private String transactionId;

    @Field(type = Keyword)
    private Set<String> roles;

    @Field(type = Keyword)
    private String icon;

    @Field(type = Keyword)
    private String assignPolicy;

    @Field(type = Keyword)
    private String dataFocusPolicy;

    @Field(type = Keyword)
    private String finishPolicy;

    public ElasticTask(Task task) {
        this.stringId = task.getStringId();
        this.processId = task.getProcessId();
        this.caseId = task.getCaseId();
        this.transitionId = task.getTransitionId();
        this.title = task.getTitle().getDefaultValue();
        this.titleSortable = title;
        this.caseTitle = task.getCaseTitle();
        this.caseTitleSortable = this.caseTitle;
        if (task.getPriority() != null)
            this.priority = task.getPriority();
        this.userId = task.getUserId();
        this.startDate = task.getStartDate();
        this.roles = task.getRoles().keySet();
    }

    public void update(ElasticTask task) {
        this.title = task.getTitle();
        this.titleSortable = title;
        this.caseTitle = task.getCaseTitle();
        this.caseTitleSortable = this.caseTitle;
        this.priority = task.getPriority();
        this.userId = task.getUserId();
        this.startDate = task.getStartDate();
        this.roles = task.getRoles();
    }
}