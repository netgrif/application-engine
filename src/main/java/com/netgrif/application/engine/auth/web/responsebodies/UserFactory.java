package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.stream.Collectors;

public class UserFactory implements IUserFactory {

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private IProcessRoleFactory processRoleFactory;

    @Override
    public User getUser(IUser user, Locale locale) {
        User result = getUser(user);

        String defaultRoleId = processRoleService.defaultRole().getStringId();
        String anonymousRoleId = processRoleService.anonymousRole().getStringId();
        result.setProcessRoles(user.getProcessRoles().stream().map(processRole -> {
            if (processRole.getStringId().equals(defaultRoleId)) {
                return new ProcessRole(processRole, locale);
            }
            if (processRole.getStringId().equals(anonymousRoleId)) {
                return new ProcessRole(processRole, locale);
            }
            return processRoleFactory.getProcessRole(processRole, locale);
        }).collect(Collectors.toSet()));

        return result;
    }

    @Override
    public User getSmallUser(IUser user) {
        return User.createSmallUser(user);
    }

    protected User getUser(IUser user) {
        return User.createUser(user);
    }
}
