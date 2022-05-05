package com.netgrif.application.engine.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.*;

@Document
public class Authority implements GrantedAuthority {

    public static final long serialVersionUID = 2839744057647464485L;

    public static final List<AuthorizingObject> defaultUserAuthorities = Arrays.asList(AuthorizingObject.FILTER, AuthorizingObject.GROUP);

    @Id
    @Getter
    private ObjectId _id;

    @NotNull
    @JsonIgnore
    @Getter
    @Setter
    private String name;

    @JsonIgnore
    @Getter
    @Setter
    private Set<String> users;

    public Authority() {
        this.users = new HashSet<>();
    }

    public Authority(String name) {
        this();
        this.name = name;
    }

    public Authority(AuthorizingObject authority) {
        this(authority.name());
    }

    public void addUser(IUser user) {
        users.add(user.getStringId());
    }

    public String getStringId() {
        return _id.toString();
    }

    @Override
    public String getAuthority() {
        return this.name;
    }

    public void setAuthority(String authority) {
        this.name = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authority authority = (Authority) o;

        return name.equals(authority.name);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + _id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean includes(AuthorizingObject authority) {
        return authority.name().equals(this.name) || authority.name().startsWith(this.name) || this.name.equals(AuthorizingObject.ADMIN.name());
    }
}
