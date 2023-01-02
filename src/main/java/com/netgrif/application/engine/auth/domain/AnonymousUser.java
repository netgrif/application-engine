package com.netgrif.application.engine.auth.domain;

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
    public LoggedUser transformToLoggedUser() {
        LoggedUser loggedUser = new LoggedUser(this.get_id().toString(), this.getEmail(), "n/a", this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        loggedUser.setAnonymous(true);
        if (!this.getProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getProcessRoles());
        if (!this.getNextGroups().isEmpty())
            loggedUser.setGroups(this.getNextGroups());

        return loggedUser;
    }
}
