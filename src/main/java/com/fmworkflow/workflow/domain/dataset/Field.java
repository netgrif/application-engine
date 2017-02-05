package com.fmworkflow.workflow.domain.dataset;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Milan on 5.2.2017.
 */
@Document
public abstract class Field {

    @Id
    private ObjectId _id;
    private String name;
    private String description;
    private int type;
    @org.springframework.data.mongodb.core.mapping.Field("show")
    private Set<String> displayForTransitions;
    @org.springframework.data.mongodb.core.mapping.Field("edit")
    private Set<String> editableForTransitions;
    @Transient
    private boolean editable;

    public Field(){
        displayForTransitions = new HashSet<>();
        editableForTransitions = new HashSet<>();
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Set<String> getDisplayForTransitions() {
        return displayForTransitions;
    }

    public void setDisplayForTransitions(Set<String> displayForTransitions) {
        this.displayForTransitions = displayForTransitions;
    }

    public Set<String> getEditableForTransitions() {
        return editableForTransitions;
    }

    public void setEditableForTransitions(Set<String> editableForTransitions) {
        this.editableForTransitions = editableForTransitions;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void modifyValue(String newValue){}

    public Field copy(){
        return this;
    }
}
