package com.netgrif.workflow.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.workflow.web.responsebodies.DataFieldsResource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;
import java.util.Set;

@Document
public class DataGroup extends PetriNetObject {

    @Getter @Setter
    @JsonIgnore
    private Set<String> data;

    @Transient
    @Getter @Setter
    private DataFieldsResource fields;

    @Getter @Setter(AccessLevel.NONE)
    private I18nString title;

    @Getter @Setter
    private String alignment;

    @Getter @Setter
    private Boolean stretch;

    public DataGroup() {
        this._id = new ObjectId();
        this.data = new LinkedHashSet<>();
    }

    public DataGroup(String title, String alignment, Boolean stretch) {
        this();
        this.title = new I18nString(title);
        this.alignment = alignment;
        this.stretch = stretch;
    }

    public void addData(String dataId) {
        data.add(dataId);
    }

    /**
     * Sets title default value
     * @param title default string value
     */
    public void setTitle(String title) {
        setTitle(new I18nString(title));
    }

    public void setTitle(I18nString title) {
        this.title = title;
    }
}