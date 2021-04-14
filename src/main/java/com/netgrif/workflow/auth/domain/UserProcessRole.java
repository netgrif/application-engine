package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Document
public class UserProcessRole {

    @Id
    @Getter @Setter
    private ObjectId _id;

    @Getter @Setter
    private String roleId;

    @Getter @Setter
    private String netId;

    @JsonIgnore
    private Set<String> users;

    public UserProcessRole() {
    }

    public UserProcessRole(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "UserProcessRole{" +
                "id=" + _id +
                ", roleId='" + roleId + '\'' +
                ", netId='" + netId + '\'' +
                '}';
    }

    public UserProcessRole(String roleId, String netId) {
        this.roleId = roleId;
        this.netId = netId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProcessRole that = (UserProcessRole) o;
        return Objects.equals(_id, that._id) && Objects.equals(roleId, that.roleId) && Objects.equals(netId, that.netId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, roleId, netId);
    }
}