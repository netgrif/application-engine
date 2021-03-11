package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.web.requestbodies.filter.SearchMetadata;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Data
@JsonRootName("filter")
public class Filter {

    private String stringId;

    private String title;

    private String description;

    private Integer visibility;

    private Author author;

    private LocalDateTime created;

    private String type;

    private List<CaseSearchRequest> caseFilterBodies;

    private List<ElasticTaskSearchRequest> taskFilterBodies;

    private MergeFilterOperation mergeOperation;

    private SearchMetadata searchMetadata;

    public Filter(com.netgrif.workflow.workflow.domain.Filter filter, Locale locale) {
        this.stringId = filter.getStringId();
        this.title = filter.getTitle().getTranslation(locale);
        this.description = filter.getDescription().getTranslation(locale);
        this.visibility = filter.getVisibility();
        this.author = filter.getAuthor();
        this.created = filter.getCreated();
        this.type = filter.getType().getStringType();
        this.mergeOperation = filter.getMergeOperation();
        this.searchMetadata = filter.getSearchMetadata();
        switch (filter.getType()) {
            case CASE:
                this.caseFilterBodies = filter.getCaseFilterBodies();
                break;
            case TASK:
                this.taskFilterBodies = filter.getTaskFilterBodies();
                break;
        }
    }
}
