package com.netgrif.application.engine.tenant;

import com.netgrif.application.engine.adapter.spring.tenant.service.TenantService;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.tenant.Tenant;
import com.netgrif.application.engine.objects.workspace.Workspace;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MockTenantService implements TenantService {


    @Override
    public Tenant save(Tenant tenant) {
        return tenant;
    }

    @Override
    public void delete(Tenant tenant) {

    }

    @Override
    public Optional<Tenant> getByCode(String tenantCode) {
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> getById(String tenantId) {
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> getByOwner(String ownerId) {
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> getByWorkspace(String workspaceId) {
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> getByRealm(String realmId) {
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> getByWorkspaceAndRealm(Workspace workspace, Realm realm) {
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> getByWorkspaceAndRealm(String workspaceId, String realmId) {
        return Optional.empty();
    }

    @Override
    public List<Tenant> getAll() {
        return List.of();
    }

    @Override
    public List<Tenant> getActiveTenants() {
        return List.of();
    }

    @Override
    public List<Tenant> getSuspendedTenants() {
        return List.of();
    }

    @Override
    public List<Tenant> getDeletedTenants() {
        return List.of();
    }

    @Override
    public boolean exists(String tenantId) {
        return false;
    }

    @Override
    public boolean existsByWorkspaceAndRealm(String workspaceId, String realmId) {
        return false;
    }
}
