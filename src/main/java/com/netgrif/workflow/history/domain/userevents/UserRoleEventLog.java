package com.netgrif.workflow.history.domain.userevents;

import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Document
public class UserRoleEventLog extends UserEventLog implements IProcessRolesEvent {

    @Getter
    private List<ProcessRole> roles;

    public UserRoleEventLog(String email, Collection<ProcessRole> roles) {
        super(email);
        this.roles = new ArrayList<>(roles);
    }
}