package com.netgrif.workflow.settings.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document
@Data
public class Preferences implements Serializable {

    @Id
    private ObjectId id;

    @Indexed
    private Long userId;

    private String locale;

    /**
     * taskViewId: [filterIds]
     */
    @Field
    private Map<String, List<String>> taskFilters;

    public Preferences(Long userId) {
        this.userId = userId;
    }

}