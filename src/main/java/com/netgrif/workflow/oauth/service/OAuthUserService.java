package com.netgrif.workflow.oauth.service;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.service.UserService;
import com.netgrif.workflow.oauth.domain.OAuthLoggedUser;
import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.domain.QOAuthUser;
import com.netgrif.workflow.oauth.domain.repositories.OAuthUserRepository;
import com.netgrif.workflow.oauth.service.interfaces.IOAuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public class OAuthUserService extends UserService implements IOAuthUserService {

    @Autowired
    protected OAuthUserRepository repository;

    @Override
    public OAuthUser saveNewOAuth(OAuthUser user) {
        addDefaultRole(user);
        addDefaultAuthorities(user);
        return repository.save(user);
    }

    @Override
    public OAuthUser findByOAuthId(String id) {
        return repository.findByOauthId(id);
    }

    @Override
    public IUser findById(String id, boolean small) {
        OAuthUser user = repository.findById(id).get();
        user.setName("todo");
        user.setSurname("todo");
        user.setEmail("todo");
        return user;
    }

    @Override
    public void encodeUserPassword(IUser user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean stringMatchesUserPassword(IUser user, String passwordToCompare) {
        throw new UnsupportedOperationException();
    }
}
