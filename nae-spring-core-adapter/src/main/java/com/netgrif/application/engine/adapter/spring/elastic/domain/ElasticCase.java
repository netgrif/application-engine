package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@NoArgsConstructor
@Document(indexName = "#{@elasticCaseIndex}")
public class ElasticCase extends com.netgrif.application.engine.objects.elastic.domain.ElasticCase {

    public ElasticCase(Case useCase) {
        super(useCase);
    }

    public void update(ElasticCase useCase) {
        super.update(useCase);
    }

    @Id
    public String getId() {
        return super.getId();
    }

    @Field(type = Keyword)
    public String getUriNodeId() {
        return super.getUriNodeId();
    }

    @Field(type = Keyword)
    public String getWorkspaceId() {
        return super.getWorkspaceId();
    }

    @Version
    public Long getVersion() {
        return super.getVersion();
    }

    @Field(type = Keyword)
    public String getStringId() {
        return super.getStringId();
    }

    @Field(type = Keyword)
    public String getProcessIdentifier() {
        return super.getProcessIdentifier();
    }

    @Field(type = Keyword)
    public String getProcessId() {
        return super.getProcessId();
    }

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    public LocalDateTime getCreationDate() {
        return super.getCreationDate();
    }

    @Field(type = Keyword)
    public String getAuthor() {
        return super.getAuthor();
    }

    @Field(type = Keyword)
    public String getMongoId() {
        return super.getMongoId();
    }

    @Field(type = Keyword)
    public String getAuthorName() {
        return super.getAuthorName();
    }

    @Field(type = Keyword)
    public String getAuthorEmail() {
        return super.getAuthorEmail();
    }

    @Field(type = Keyword)
    public Set<String> getTaskIds() {
        return super.getTaskIds();
    }

    @Field(type = Keyword)
    public Set<String> getTaskMongoIds() {
        return super.getTaskMongoIds();
    }

    @Field(type = Keyword)
    public Set<String> getEnabledRoles() {
        return super.getEnabledRoles();
    }

    @Field(type = Keyword)
    public Set<String> getViewRoles() {
        return super.getViewRoles();
    }

    @Field(type = Keyword)
    public Set<String> getViewUserRefs() {
        return super.getViewUserRefs();
    }

    @Field(type = Keyword)
    public Set<String> getNegativeViewRoles() {
        return super.getNegativeViewRoles();
    }

    @Field(type = Keyword)
    public Set<String> getNegativeViewUsers() {
        return super.getNegativeViewUsers();
    }
}
