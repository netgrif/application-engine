package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UserFactory implements IUserFactory {

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private IProcessRoleFactory processRoleFactory;

    @Override
    public User getUser(IUser user, Locale locale) {
        User result = User.createUser(user);

        String defaultRoleId = processRoleService.defaultRole().getStringId();
//        Map<String, UserProcessRole> userProcessRoles = user.getUserProcessRoles().stream().collect(Collectors.toMap(UserProcessRole::getRoleId, r -> r));
        result.setProcessRoles(user.getProcessRoles().stream().map(processRole -> {
            if (processRole.getStringId().equals(defaultRoleId)) {
                return new ProcessRole(processRole, locale);
            }
            /*UserProcessRole userProcessRole = userProcessRoles.get(processRole.getStringId());
            if (userProcessRole == null) {
                throw new IllegalStateException("User has ProcessRole without UserProcessRole!");
            }*/
            return processRoleFactory.getProcessRole(processRole, locale);
        }).collect(Collectors.toSet()));

        return result;
    }

    @Override
    public User getSmallUser(IUser user) {
        return User.createSmallUser(user);
    }
}
