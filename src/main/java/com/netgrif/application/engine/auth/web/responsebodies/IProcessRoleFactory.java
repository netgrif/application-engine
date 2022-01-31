package com.netgrif.application.engine.auth.web.responsebodies;

import java.util.Locale;

public interface IProcessRoleFactory {
    ProcessRole getProcessRole(com.netgrif.application.engine.petrinet.domain.roles.ProcessRole role, Locale locale);
}
