package com.netgrif.workflow.auth.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "organizations")
    private Set<User> users;

    public Organization() {
        users = new HashSet<>();
    }

    public Organization(String name) {
        this();
        this.name = name;
    }

    public void addUser(User user){
        this.users.add(user);
    }
}
