package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "authority")
public class Authority implements GrantedAuthority {

    public static final long serialVersionUID = 2839744057647464485L;

    public static final String PERMISSION = "PERM_";
    public static final String ROLE = "ROLE_";

    public static final String admin = ROLE + "ADMIN";
    public static final String system = ROLE + "SYSTEM";
    public static final String user = ROLE + "USER";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @NotNull
    @Column(unique = true)
    @JsonIgnore
    @Getter
    @Setter
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "authorities")
    @Getter
    @Setter
    private Set<User> users;

    public Authority() {
    }

    public Authority(String name) {
        this.name = name;
    }

    public static Authority createRole(String name) {
        return new Authority(ROLE + name);
    }

    public static Authority createPermission(String name) {
        return new Authority(PERMISSION + name);
    }

    public void addUser(User user) {
        users.add(user);
    }

    @Override
    public String getAuthority() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authority authority = (Authority) o;

        return name.equals(authority.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
