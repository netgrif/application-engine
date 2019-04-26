package com.netgrif.workflow.elastic.domain;

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
@Document(indexName = "task", type = "task")
public class ElasticTask {

    @Id
    private String id;

    @Field(type = Keyword)
    private String processId;

    @Field(type = Keyword)
    private String caseId;

    @Field(type = Keyword)
    private String transitionId;

    private String title;

    private String caseTitle;

    private Integer priority;

    private Long userId;

    private LocalDateTime startDate;

    private Set<String> roles;

    public ElasticTask(Task task) {
        this.id = task.getStringId();
        this.processId = task.getProcessId();
        this.caseId = task.getCaseId();
        this.transitionId = task.getTransitionId();
        this.title = task.getTitle().getDefaultValue();
        this.caseTitle = task.getCaseTitle();
        this.priority = task.getPriority();
        this.userId = task.getUserId();
        this.startDate = task.getStartDate();
        this.roles = task.getRoles().keySet();
    }
}