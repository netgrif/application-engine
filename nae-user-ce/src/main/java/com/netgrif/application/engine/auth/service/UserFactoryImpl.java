package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.stream.Collectors;

public class UserFactoryImpl implements UserFactory {

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ProcessRoleFactory processRoleFactory;

    @Override
    public User getUser(AbstractUser user, Locale locale) {
        User result = getUser(user);

        if (user.getProcessRoles().isEmpty()) {
            return result;
        }

        String defaultRoleId = processRoleService.getDefaultRole().getStringId();
        String anonymousRoleId = processRoleService.getAnonymousRole().getStringId();
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

    protected User getUser(AbstractUser user) {
        return User.createUser(user);
    }
}
