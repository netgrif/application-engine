package com.netgrif.application.engine.adapter.spring.auth.domain.mapper;

import com.netgrif.application.engine.adapter.spring.auth.domain.User;
import com.netgrif.application.engine.objects.auth.domain.Author;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import org.bson.types.ObjectId;

public class UserAuthorMapper {

    public static Author toAuthor(IUser user) {
        if (user == null) return null;
        return new Author(user.getStringId(), user.getEmail(), user.getFullName());
    }

    public static IUser toUser(Author author) {
        if (author == null) return null;
        User user = new User(new ObjectId(author.getId()));
        user.setEmail(author.getEmail());
        return user;
    }
}
