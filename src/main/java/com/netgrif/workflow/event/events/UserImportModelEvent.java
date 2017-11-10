package com.netgrif.workflow.event.events;

import com.netgrif.workflow.auth.domain.LoggedUser;
import lombok.Getter;

import java.io.File;

public class UserImportModelEvent extends Event {

    @Getter
    private File model;

    public UserImportModelEvent(Object user, File model) {
        super(user);
        this.model = model;
    }

    @Override
    public String getMessage() {
        return "User " +
                ((LoggedUser) this.source).getUsername() +
                " imported new model " +
                this.model.getName();
    }
}