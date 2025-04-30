package com.netgrif.application.engine.adapter.spring.petrinet.service;

import com.netgrif.application.engine.adapter.spring.petrinet.dto.ProcessRole;

import java.util.Locale;

public interface ProcessRoleFactory {
    ProcessRole getProcessRole(com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole role, Locale locale);
}
