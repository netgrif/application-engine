package com.netgrif.workflow.admin.service.interfaces;

import com.netgrif.workflow.admin.service.AdminActionException;
import com.netgrif.workflow.auth.domain.LoggedUser;

public interface IAdminService {

    String run(String code, LoggedUser user) throws AdminActionException;
}