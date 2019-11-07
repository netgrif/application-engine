package com.netgrif.workflow.settings.service;

import com.netgrif.workflow.settings.domain.RolePreferences;
import org.springframework.stereotype.Service;

@Service
public interface IRolePreferencesService {
    RolePreferences get(Long processRoleId);

    RolePreferences save(RolePreferences preferences);
}
