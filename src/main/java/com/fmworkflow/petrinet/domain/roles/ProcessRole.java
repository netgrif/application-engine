package com.fmworkflow.petrinet.domain.roles;

import com.fmworkflow.auth.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToMany;
import java.util.Set;

@Document
@Entity
public class ProcessRole {
    @Id
    private ObjectId _id;
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Transient
    private Long id;
    @javax.persistence.Transient
    private String name;
    @javax.persistence.Transient
    private String description;
    @Transient
    @ManyToMany(mappedBy = "processRoles")
    private Set<User> users;

    public ProcessRole() {
        _id = new ObjectId();
    }

    public String getObjectId() {
        return _id.toString();
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
}