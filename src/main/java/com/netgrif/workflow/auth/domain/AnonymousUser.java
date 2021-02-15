package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "anonymous_user")
public class AnonymousUser extends User{

    public AnonymousUser() {
        super();
    }

    public AnonymousUser(Long id) {
        super(id);
    }

    public AnonymousUser(String email, String password, String name, String surname) {
        super(email, password, name, surname);
    }
}
