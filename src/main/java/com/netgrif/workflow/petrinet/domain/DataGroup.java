package com.netgrif.workflow.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.web.responsebodies.DataFieldsResource;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document
public class DataGroup extends PetriNetObject {

    @Getter @Setter
    @JsonIgnore
    private Set<String> data;

    @Transient
    @Getter @Setter
    private DataFieldsResource fields;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String alignment;

    public DataGroup() {
        this._id = new ObjectId();
        this.data = new LinkedHashSet<>();
    }

    public DataGroup(String title, String alignment) {
        this();
        this.title = title;
        this.alignment = alignment;
    }

    public void addData(String dataId) {
        data.add(dataId);
    }
}