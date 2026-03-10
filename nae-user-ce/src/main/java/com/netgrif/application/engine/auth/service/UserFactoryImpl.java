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

        com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole defaultRole = processRoleService.getDefaultRole();
        String defaultRoleId = defaultRole == null ? null : defaultRole.getStringId();
        com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole anonymousRole = processRoleService.getAnonymousRole();
        String anonymousRoleId = anonymousRole == null ? null : anonymousRole.getStringId();
        result.setProcessRoles(user.getProcessRoles().stream().map(processRole -> {
            if (processRole.getStringId().equals(defaultRoleId)) { // todo 2072 process roles in user should not be the same object as in cache
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
