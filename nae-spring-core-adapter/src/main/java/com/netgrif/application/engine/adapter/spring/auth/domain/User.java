package com.netgrif.application.engine.adapter.spring.auth.domain;


import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.LoggedUserMapper;
import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.UserAuthorMapper;
import com.netgrif.application.engine.objects.auth.domain.Author;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.querydsl.core.annotations.QueryEntity;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
@QueryEntity
public class User extends com.netgrif.application.engine.objects.auth.domain.User {

    public User() {
        super();
    }

    public User(ObjectId id) {
        super(id);
    }

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
