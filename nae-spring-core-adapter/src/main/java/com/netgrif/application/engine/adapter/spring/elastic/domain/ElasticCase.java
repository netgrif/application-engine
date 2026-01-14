package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@NoArgsConstructor
@Document(indexName = "#{@elasticCaseIndex}", createIndex = false)
public class ElasticCase extends com.netgrif.application.engine.objects.elastic.domain.ElasticCase {

    public ElasticCase(Case useCase) {
        super(useCase);
    }

    public void update(ElasticCase useCase) {
        super.update(useCase);
    }

    @Id
    @Field(type = Keyword)
    public String getId() {
        return super.getId();
    }

    @MultiField(
            mainField = @Field(type = Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = Keyword)
            })
    public String getTitle() {
        return super.getTitle();
    }

    @Field(type = Keyword)
    public String getVisualId() {
        return super.getVisualId();
    }

    @Field(type = Keyword)
    public String getCaseId() {
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

    @Field(type = Keyword)
    public String getWorkspaceId() {
        return super.getWorkspaceId();
    }

    @Field(type = Date, format = DateFormat.date_hour_minute_second_millis)
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

    @MultiField(
            mainField = @Field(type = Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = Keyword)
            })
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
