package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.Set;

public class Group extends com.netgrif.application.engine.objects.auth.domain.Group {

    protected Group() {
        super();
    }

    public Group(ObjectId id) {
        super(id);
    }

    public Group(String identifier, String realmId) {
        super(identifier, realmId);
    }

    @Override
    @Transient
    public Set<ProcessRole> getProcessRoles() {
        return super.getProcessRoles();
    }

    @Override
    @Transient
    public Set<Authority> getAuthoritySet() {
        return super.getAuthoritySet();
    }
}
