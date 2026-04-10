package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.petrinet.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.auth.web.responsebodies.UserDto;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.Locale;
import java.util.stream.Collectors;

public class UserFactoryImpl implements UserFactory {

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ProcessRoleFactory processRoleFactory;

    @Autowired
    private GroupService groupService;

    @Override
    public UserDto getUser(AbstractUser user, Locale locale) {
        UserDto result = UserDto.createUser(user, groupService.findAllByIds(user.getGroupIds(), Pageable.unpaged()).stream().map(group -> GroupDto.fromGroup(group, locale)).collect(Collectors.toList()));

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

}
