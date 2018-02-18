package com.netgrif.workflow.auth.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @NotNull
    @Column(unique = true)
    @Getter @Setter
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "organizations", fetch = FetchType.LAZY)
    @Getter @Setter
    private Set<User> users;

    public Organization() {
        users = new HashSet<>();
    }

    public Organization(String name) {
        this();
        this.name = name;
    }

    public Organization(Long id) {
        this();
        this.id = id;
    }

    public void addUser(User user){
        this.users.add(user);
    }

    public Long getEntityId(){
        return id;
    }
}
