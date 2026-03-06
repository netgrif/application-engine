package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.tenant.exception.TenantConflictException;
import com.netgrif.application.engine.adapter.spring.tenant.exception.TenantNotFoundException;
import com.netgrif.application.engine.auth.repository.TenantRepository;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.tenant.Tenant;
import com.netgrif.application.engine.objects.tenant.TenantConstants;
import com.netgrif.application.engine.objects.workspace.Workspace;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository repository;


    @Override
    public Tenant save(Tenant tenant) {
        return repository.save(tenant);
    }

    @Override
    public Tenant getAdminTenant() {
        return getById(getAdminTenantId()).orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    }

    @Override
    public String getAdminTenantId() {
        return TenantConstants.AdminTenant.ID;
    }

    @Override
    public void delete(Tenant tenant) {
        repository.delete(tenant);
    }

    @Override
    public void addRealm(String tenantId, Realm realm) {
        getById(tenantId).ifPresentOrElse((tenant) -> {
                    if (tenant.getDefaultRealmId().isEmpty() && realm.isDefaultRealm()) {
                        tenant.addRealm(realm);
                    } else {
                        throw new TenantConflictException("Cannot set more than one default realm per tenant");
                    }
                    save(tenant);
                },
                () -> {
                    throw new TenantNotFoundException("Tenant not found");
                });
    }

    @Override
    public void removeRealm(String tenantId, String realmId) {
        getById(tenantId).ifPresentOrElse((tenant) -> {
                    tenant.removeRealm(realmId);
                    save(tenant);
                },
                () -> {
                    throw new TenantNotFoundException("Tenant not found");
                });
    }

    @Override
    public void addWorkspace(String tenantId, Workspace workspace) {
        getById(tenantId).ifPresentOrElse((tenant) -> {
                    tenant.addWorkspace(workspace.getId());
                    save(tenant);
                },
                () -> {
                    throw new TenantNotFoundException("Tenant not found");
                });
    }

    @Override
    public void removeWorkspace(String tenantId, String workspaceId) {
        getById(tenantId).ifPresentOrElse((tenant) -> {
                    tenant.removeWorkspace(workspaceId);
                    save(tenant);
                },
                () -> {
                    throw new TenantNotFoundException("Tenant not found");
                });
    }

    @Override
    public Optional<Tenant> getByCode(String tenantCode) {
        return repository.findTenantByTenantCode(tenantCode);
    }

    @Override
    public Optional<Tenant> getById(String tenantId) {
        return repository.findById(tenantId);
    }

    @Override
    public Optional<Tenant> getByOwner(String owner) {
        return repository.findByOwner(owner);
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
    public boolean exists(String tenantId) {
        return repository.existsById(tenantId);
    }

    @Override
    public boolean existsByWorkspaceAndRealm(String workspaceId, String realmId) {
        return repository.existsByWorkspacesContainingAndRealmsContaining(workspaceId, realmId);
    }

}
