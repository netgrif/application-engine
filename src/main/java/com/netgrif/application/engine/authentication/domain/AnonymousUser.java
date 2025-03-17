package com.netgrif.application.engine.authentication.domain;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class AnonymousUser extends User {

    public AnonymousUser() {
        super();
    }

    public AnonymousUser(ObjectId id) {
        super(id);
    }

    public AnonymousUser(String email, String password, String name, String surname) {
        super(email, password, name, surname);
    }

    @Override
    public Identity transformToLoggedUser() {
        Identity identity = new Identity(this.getId().toString(), this.getEmail(), "n/a", this.getAuthorities());
        identity.setFullName(this.getFullName());
        identity.setAnonymous(true);
        // todo 2058
//        if (!this.getRoles().isEmpty())
//            loggedUser.parseRoles(this.getRoles());
        if (!this.getNextGroups().isEmpty())
            identity.setGroups(this.getNextGroups());

        return identity;
    }
}
