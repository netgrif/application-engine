package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
public class Filter {

    public static final int VISIBILITY_PUBLIC = 2;
    public static final int VISIBILITY_GROUP = 1;
    public static final int VISIBILITY_PRIVATE = 0;

    @Id
    private ObjectId _id;

    private I18nString title;

    private I18nString description;

    private Integer visibility;

    private Author author;

    private LocalDateTime created;

    private FilterType type;

    private MergeFilterOperation mergeOperation;

    private List<CaseSearchRequest> caseFilterBodies;

    private List<ElasticTaskSearchRequest> taskFilterBodies;

    private Filter(I18nString title, I18nString description, Integer visibility, Author author, MergeFilterOperation mergeOperation) {
        this.created = LocalDateTime.now();
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.author = author;
        this.mergeOperation = mergeOperation;
    }

    public String getStringId() {
        return this._id.toString();
    }

    public static Filter createCaseFilter(I18nString title, I18nString description, Integer visibility, Author author, MergeFilterOperation mergeOperation, List<CaseSearchRequest> caseFilterBodies) {
        Filter f = new Filter(title, description, visibility, author, mergeOperation);
        f.type = FilterType.CASE;
        f.caseFilterBodies = caseFilterBodies;
        return f;
    }

    public static Filter createTaskFilter(I18nString title, I18nString description, Integer visibility, Author author, MergeFilterOperation mergeOperation, List<ElasticTaskSearchRequest> taskFilterBodies) {
        Filter f = new Filter(title, description, visibility, author, mergeOperation);
        f.type = FilterType.TASK;
        f.taskFilterBodies = taskFilterBodies;
        return f;
    }
}
