package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;

import java.util.Locale;

public interface ProcessRoleFactory {
    ProcessRoleDto getProcessRole(com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole role, Locale locale);
}
