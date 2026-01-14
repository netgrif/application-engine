package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.workflow.domain.Task;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Flattened;
import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@NoArgsConstructor
@Document(indexName = "#{@elasticTaskIndex}", createIndex = false)
public class ElasticTask extends com.netgrif.application.engine.objects.elastic.domain.ElasticTask {

    public ElasticTask(Task task) {
        super(task);
    }

    public void update(ElasticTask task) {
        super.update(task);
    }

    @Id
    @Override
    public String getId() {
        return super.getId();
    }

    @Field(type = Keyword)
    @Override
    public String getProcessId() {
        return super.getProcessId();
    }

    @Field(type = Keyword)
    @Override
    public String getCaseId() {
        return super.getCaseId();
    }

    @Field(type = Keyword)
    @Override
    public String getTaskId() {
        return super.getTaskId();
    }

    @Field(type = Keyword)
    @Override
    public String getTransitionId() {
        return super.getTransitionId();
    }

    @Field(type = Keyword)
    @Override
    public String getWorkspaceId() {
        return super.getWorkspaceId();
    }

    @Field(type = Keyword)
    @Override
    public String getTitleSortable() {
        return super.getTitleSortable();
    }

    @Field(type = Keyword)
    @Override
    public String getCaseColor() {
        return super.getCaseColor();
    }

    @Field(type = Keyword)
    @Override
    public String getCaseTitleSortable() {
        return super.getCaseTitleSortable();
    }

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @Override
    public LocalDateTime getStartDate() {
        return super.getStartDate();
    }

    @Field(type = Keyword)
    @Override
    public String getTransactionId() {
        return super.getTransactionId();
    }

    @Field(type = Flattened)
    public Map<String, Map<String, Boolean>> getUsers() {
        return super.getUsers();
    }

    @Field(type = Flattened)
    public Map<String, Map<String, Boolean>> getUserRefs() {
        return super.getUserRefs();
    }

    @Field(type = Flattened)
    public Map<String, Map<String, Boolean>> getRoles() {
        return super.getRoles();
    }

    @Field(type = Keyword)
    @Override
    public Set<String> getViewUserRefs() {
        return super.getViewUserRefs();
    }

    @Field(type = Keyword)
    @Override
    public Set<String> getViewRoles() {
        return super.getViewRoles();
    }

    @Field(type = Keyword)
    @Override
    public Set<String> getNegativeViewRoles() {
        return super.getNegativeViewRoles();
    }

    @Field(type = Keyword)
    @Override
    public Set<String> getViewUsers() {
        return super.getViewUsers();
    }

    @Field(type = Keyword)
    @Override
    public Set<String> getNegativeViewUsers() {
        return super.getNegativeViewUsers();
    }

    @Field(type = Keyword)
    @Override
    public String getIcon() {
        return super.getIcon();
    }

    @Field(type = Keyword)
    @Override
    public String getAssignPolicy() {
        return super.getAssignPolicy();
    }

    @Field(type = Keyword)
    @Override
    public String getDataFocusPolicy() {
        return super.getDataFocusPolicy();
    }

    @Field(type = Keyword)
    @Override
    public String getFinishPolicy() {
        return super.getFinishPolicy();
    }
}
