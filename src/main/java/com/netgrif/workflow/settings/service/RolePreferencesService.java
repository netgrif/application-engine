package com.netgrif.workflow.settings.service;

import com.netgrif.workflow.settings.domain.RolePreferences;
import com.netgrif.workflow.settings.domain.RolePreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolePreferencesService implements IRolePreferencesService {

    @Autowired
    private RolePreferencesRepository repository;

    @Override
    public RolePreferences get(Long processRoleId) {
        return repository.findByProcessRoleId(processRoleId);
    }

    @Override
    public RolePreferences save(RolePreferences preferences) {
        return repository.save(preferences);
    }
}