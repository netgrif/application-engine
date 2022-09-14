package com.netgrif.application.engine.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.*;

@Document
public class Authority implements GrantedAuthority {

    public static final long serialVersionUID = 2839744057647464485L;

    public static final String defaultAdminAuthority = "*";
    public static final List<String> defaultUserAuthorities = Arrays.asList("FILTER_*",
            AuthorizingObject.GROUP_VIEW_OWN.name());
    public static final List<String> defaultAnonymousAuthorities = Collections.emptyList();

    @Id
    @NotNull
    @JsonIgnore
    @Getter
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

    public void removeUser(IUser user) {
        users.remove(user.getStringId());
    }

    public String getStringId() {
        return name;
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
                "id=" + name +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
