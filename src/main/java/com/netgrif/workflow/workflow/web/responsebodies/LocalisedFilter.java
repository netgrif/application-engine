package com.netgrif.workflow.workflow.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.workflow.domain.Filter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Locale;

@Data
@JsonRootName("filter")
public class LocalisedFilter {

    private String stringId;

    private String title;

    private String description;

    private Integer visibility;

    private Author author;

    private LocalDateTime created;

    private String type;

    private String query;

    private String readableQuery;

    public LocalisedFilter(Filter filter, Locale locale) {
        this.stringId = filter.getStringId();
        this.title = filter.getTitle().getTranslation(locale);
        this.description = filter.getDescription().getTranslation(locale);
        this.visibility = filter.getVisibility();
        this.author = filter.getAuthor();
        this.created = filter.getCreated();
        this.type = filter.getType();
        this.query = filter.getQuery();
        this.readableQuery = filter.getReadableQuery();
    }


}
