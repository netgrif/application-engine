package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
public class Filter {

    public static final int VISIBILITY_PUBLIC = 2;
    public static final int VISIBILITY_GROUP = 1;
    public static final int VISIBILITY_PRIVATE = 0;

    public static final String TYPE_TASK = "task";
    public static final String TYPE_CASE = "case";

    @Id
    private ObjectId _id;

    private I18nString title;

    private I18nString description;

    private Integer visibility;

    private Author author;

    private LocalDateTime created;

    private String type;

    private String query;

    public Filter() {
        this.created = LocalDateTime.now();
    }

    public Filter(I18nString title, I18nString description, Integer visibility, Author author, String type, String query) {
        this();
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.author = author;
        this.type = type;
        this.query = query;
    }

    public String getStringId(){
        return this._id.toString();
    }
}
