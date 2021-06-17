package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
public class UserProcessRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @Column(unique = true)
    @Getter
    @Setter
    private String roleId;

    @Getter
    @Setter
    private String netId;

    @JsonIgnore
    @ManyToMany(mappedBy = "userProcessRoles")
    private Set<User> users;

    public UserProcessRole() {
    }

    public UserProcessRole(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "UserProcessRole{" +
                "id=" + id +
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
        return Objects.equals(id, that.id) && Objects.equals(roleId, that.roleId) && Objects.equals(netId, that.netId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roleId, netId);
    }
}