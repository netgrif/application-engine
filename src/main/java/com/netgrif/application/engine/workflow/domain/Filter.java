package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Document
@Data
public class Filter {

    public static final int VISIBILITY_PUBLIC = 2;
    public static final int VISIBILITY_GROUP = 1;
    public static final int VISIBILITY_PRIVATE = 0;

    public static final String TYPE_TASK = "Task";
    public static final String TYPE_CASE = "Case";

    @Id
    private ObjectId _id;

    private I18nString title;

    private I18nString description;

    private Integer visibility;

    private Author author;

    private LocalDateTime created;

    private String type;

    private String query;

    private MergeFilterOperation mergeOperation;

    public Filter() {
        this.created = LocalDateTime.now();
    }

    public Filter(I18nString title, I18nString description, Integer visibility, Author author, String type, String query, MergeFilterOperation mergeOperation) {
        this();
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.author = author;
        this.type = type;
        this.query = query;
        this.mergeOperation = mergeOperation;
    }

    public String getStringId() {
        return this._id.toString();
    }
}
