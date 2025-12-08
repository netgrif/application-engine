package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;
import com.netgrif.application.engine.objects.dto.response.user.UserDto;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class UserFactoryImpl implements UserFactory {

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ProcessRoleFactory processRoleFactory;

    @Override
    public UserDto getUser(AbstractUser user, Locale locale) {
        return getUserWithImpersonation(user, null, locale);
    }

    @Override
    public UserDto getUserWithImpersonation(AbstractUser user, AbstractUser impersonated, Locale locale) {
        String defaultRoleId = processRoleService.getDefaultRole().getStringId();
        String anonymousRoleId = processRoleService.getAnonymousRole().getStringId();
        Set<ProcessRoleDto> roles = user.getProcessRoles().stream().map(processRole -> {
            if (processRole.getStringId().equals(defaultRoleId)) {
                return new ProcessRoleDto(processRole, locale);
            }
            if (processRole.getStringId().equals(anonymousRoleId)) {
                return new ProcessRoleDto(processRole, locale);
            }
            return processRoleFactory.getProcessRole(processRole, locale);
        }).collect(Collectors.toSet());
        UserDto impersonatedUser = null;
        if(impersonated != null) {
            impersonatedUser = this.getUserWithImpersonation(impersonated, null, locale);
        }
        return UserDto.fromAbstractUser(user, impersonatedUser, roles);
    }
}
