package com.netgrif.application.engine.adapter.spring.auth.domain;


import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.LoggedUserMapper;
import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.UserAuthorMapper;
import com.netgrif.application.engine.objects.auth.domain.Author;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.Set;

@QueryEntity
public class User extends com.netgrif.application.engine.objects.auth.domain.User {

    public User() {
        super();
    }

    public User(ObjectId id) {
        super(id);
    }

    @Getter
    @Setter
    @Transient
    private String workspaceId;

    @Override
    public LoggedUser transformToLoggedUser() {
        return LoggedUserMapper.toLoggedUser(this);
    }

    @Override
    public Author transformToAuthor() {
        return UserAuthorMapper.toAuthor(this);
    }

    @Override
    @Transient
    public Set<Group> getGroups() {
        return super.getGroups();
    }
}
