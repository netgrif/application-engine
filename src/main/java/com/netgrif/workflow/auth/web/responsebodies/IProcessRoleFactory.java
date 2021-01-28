package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.auth.domain.UserProcessRole;

import java.util.Locale;

public interface IProcessRoleFactory {
    ProcessRole getProcessRole(com.netgrif.workflow.petrinet.domain.roles.ProcessRole role, UserProcessRole userProcessRole, Locale locale);
}
