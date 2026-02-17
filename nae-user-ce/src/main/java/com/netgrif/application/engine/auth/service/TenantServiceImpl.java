package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.tenant.domain.AdminTenant;
import com.netgrif.application.engine.adapter.spring.tenant.exception.TenantConflictException;
import com.netgrif.application.engine.adapter.spring.tenant.exception.TenantNotFoundException;
import com.netgrif.application.engine.adapter.spring.tenant.service.TenantService;
import com.netgrif.application.engine.auth.repository.TenantRepository;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.tenant.Tenant;
import com.netgrif.application.engine.objects.workspace.Workspace;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository repository;

    @Autowired
    private AdminTenant adminTenant;

    @Override
    public Tenant save(Tenant tenant) {
        if (isAdminTenant(tenant)) return adminTenant;
        return repository.save(tenant);
    }

    @Override
    public void delete(Tenant tenant) {
        if (isAdminTenant(tenant)) throw new TenantConflictException("Cannot delete admin tenant");
        repository.delete(tenant);
    }

    @Override
    public void addRealm(String tenantId, String realmId) {
        getById(tenantId).ifPresentOrElse((tenant) -> {
                    tenant.addRealm(realmId);
                    save(tenant);
                },
                () -> {
                    throw new TenantNotFoundException("Tenant not found");
                });
    }

    @Override
    public void addWorkspace(String tenantId, String workspaceId) {
        getById(tenantId).ifPresentOrElse((tenant) -> {
                    tenant.addWorkspace(workspaceId);
                    save(tenant);
                },
                () -> {
                    throw new TenantNotFoundException("Tenant not found");
                });
    }

    @Override
    public Optional<Tenant> getByCode(String tenantCode) {
        if (AdminTenant.ADMIN_TENANT_CODE.equals(tenantCode)) return Optional.of(adminTenant);
        return repository.findTenantByTenantCode(tenantCode);
    }

    @Override
    public Optional<Tenant> getById(String tenantId) {
        if (AdminTenant.ADMIN_TENANT_ID.equals(tenantId)) return Optional.of(adminTenant);
        return repository.findById(tenantId);
    }

    @Override
    public Optional<Tenant> getByOwner(String ownerId) {
        if (adminTenant.getOwner().getId().equals(ownerId)) return Optional.of(adminTenant);
        return repository.findByOwner_Id(ownerId);

    }

    @Override
    public Optional<Tenant> getByWorkspace(String workspaceId) {
        return repository.findTenantByWorkspacesContaining(workspaceId);
    }

    @Override
    public Optional<Tenant> getByRealm(String realmId) {
        return repository.findTenantByRealmsContaining(realmId);
    }

    @Override
    public Optional<Tenant> getByWorkspaceAndRealm(Workspace workspace, Realm realm) {
        return repository.findByWorkspacesContainingAndRealmsContaining(workspace.getId(), realm.getName());
    }

    @Override
    public Optional<Tenant> getByWorkspaceAndRealm(String workspaceId, String realmId) {
        return repository.findByWorkspacesContainingAndRealmsContaining(workspaceId, realmId);
    }

    @Override
    public List<Tenant> getAll() {
        return repository.findAll();
    }


    @Override
    public List<Tenant> getActiveTenants() {
        return repository.findTenantsByActiveIsTrue();
    }

    @Override
    public List<Tenant> getSuspendedTenants() {
        return repository.findTenantsBySuspendedIsTrue();
    }

    @Override
    public List<Tenant> getDeletedTenants() {
        return repository.findTenantsByDeletedIsTrue();
    }

    @Override
    public boolean isAdminTenant(Tenant tenant) {
        return adminTenant.equals(tenant);
    }

    @Override
    public boolean exists(String tenantId) {
        return repository.existsById(tenantId);
    }

    @Override
    public boolean existsByWorkspaceAndRealm(String workspaceId, String realmId) {
        return repository.existsByWorkspacesContainingAndRealmsContaining(workspaceId, realmId);
    }

}
