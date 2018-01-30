package com.netgrif.workflow.petrinet.domain.roles;

import com.netgrif.workflow.petrinet.domain.I18nString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ProcessRole {

    public static final String DEFAULT_ROLE = "default";

    @Id
    private ObjectId _id;

    private I18nString name;

    private String description;

    public ProcessRole() {
        _id = new ObjectId();
    }

    public String getStringId() {
        return _id.toString();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public I18nString getName() {
        return name;
    }

    public void setName(String name) {
        setName(new I18nString(name));
    }

    public void setName(I18nString name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}