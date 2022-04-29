package com.netgrif.application.engine.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Document
public class Authority implements GrantedAuthority {

    public static final long serialVersionUID = 2839744057647464485L;

    public static final List<AuthorityEnum> defaultUserAuthorities = Arrays.asList(AuthorityEnum.FILTER, AuthorityEnum.GROUP);

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
    }

    public Authority(String name) {
        this.name = name;
    }

    public Authority(AuthorityEnum authority) {
        this.name = authority.name();
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

    public boolean inDomain(AuthorityEnum domain) {
        return this.name.startsWith(domain.name()) || this.name.equals(AuthorityEnum.ADMIN.name());
    }
}
