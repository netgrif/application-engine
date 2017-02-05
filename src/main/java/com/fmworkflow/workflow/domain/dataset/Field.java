package com.fmworkflow.workflow.domain.dataset;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Milan on 5.2.2017.
 */
@Document
public abstract class Field {

    @Id
    private ObjectId _id;
    private String name;
    private String description;
    @org.springframework.data.mongodb.core.mapping.Field("show")
    private List<ObjectId> displayForTransitions;
    @org.springframework.data.mongodb.core.mapping.Field("edit")
    private List<ObjectId> editableForTransitions;

    public Field(){
        displayForTransitions = new ArrayList<>();
        editableForTransitions = new ArrayList<>();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ObjectId> getDisplayForTransitions() {
        return displayForTransitions;
    }

    public void setDisplayForTransitions(List<ObjectId> displayForTransitions) {
        this.displayForTransitions = displayForTransitions;
    }

    public List<ObjectId> getEditableForTransitions() {
        return editableForTransitions;
    }

    public void setEditableForTransitions(List<ObjectId> editableForTransitions) {
        this.editableForTransitions = editableForTransitions;
    }
}
