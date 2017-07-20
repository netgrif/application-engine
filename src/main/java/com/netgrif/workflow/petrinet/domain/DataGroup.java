package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedList;
import java.util.List;

@Document
public class DataGroup extends PetriNetObject {

    @Getter
    @Setter
    private List<String> data;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String alignment;

    public DataGroup() {
        this._id = new ObjectId();
        this.data = new LinkedList<>();
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