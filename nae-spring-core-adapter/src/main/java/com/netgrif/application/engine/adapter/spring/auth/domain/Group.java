package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.Set;

@QueryEntity
public class Group extends com.netgrif.application.engine.objects.auth.domain.Group {

    public Group() {
        super();
    }

    public Group(String identifier, String realmId) {
        super(identifier, realmId);
    }

    @Override
    @Transient
    public Set<IUser> getMembers() {
        return super.getMembers();
    }

    @Override
    @Transient
    public Set<com.netgrif.application.engine.objects.auth.domain.Group> getGroups() {
        return super.getGroups();
    }
}
