package com.netgrif.workflow.auth.web.responsebodies;

import java.util.Locale;

public interface IProcessRoleFactory {
    ProcessRole getProcessRole(com.netgrif.workflow.petrinet.domain.roles.ProcessRole role, Locale locale);
}
