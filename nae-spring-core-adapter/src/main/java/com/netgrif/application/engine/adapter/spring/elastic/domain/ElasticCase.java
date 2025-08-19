package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.Flattened;
import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Document(indexName = "#{@elasticCaseIndex}")
public class ElasticCase extends com.netgrif.application.engine.objects.elastic.domain.ElasticCase {

    public ElasticCase() {
        super();
    }

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

    @Version
    public Long getVersion() {
        return super.getVersion();
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
    public String getAuthorRealm() {
        return super.getAuthorRealm();
    }

    @Field(type = Keyword)
    public String getAuthorName() {
        return super.getAuthorName();
    }

    @Field(type = Keyword)
    public String getAuthorUsername() {
        return super.getAuthorUsername();
    }

    @Field(type = Keyword)
    public Set<String> getTaskIds() {
        return super.getTaskIds();
    }

    @Field(type = Keyword)
    public Set<String> getTaskMongoIds() {
        return super.getTaskMongoIds();
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
    public Map<String, Map<String, Boolean>> getPermissions() {
        return super.getPermissions();
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
