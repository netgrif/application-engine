package com.netgrif.application.engine.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
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

    private String query;

    private MergeFilterOperation mergeOperation;

    public Filter(com.netgrif.application.engine.workflow.domain.Filter filter, Locale locale) {
        this.stringId = filter.getStringId();
        this.title = filter.getTitle().getTranslation(locale);
        this.description = filter.getDescription().getTranslation(locale);
        this.visibility = filter.getVisibility();
        this.author = filter.getAuthor();
        this.created = filter.getCreated();
        this.type = filter.getType();
        this.query = filter.getQuery();
        this.mergeOperation = filter.getMergeOperation();
    }


}
