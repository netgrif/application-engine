package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.tenant.Tenant;
import com.netgrif.application.engine.objects.workspace.Workspace;

import java.util.List;
import java.util.Optional;

public interface TenantService {

    Tenant save(Tenant tenant);

    void delete(Tenant tenant);

    void addRealm(String tenantId, Realm realm);

    void removeRealm(String tenantId, String realmId);

    void addWorkspace(String tenantId, Workspace workspace);

    void removeWorkspace(String tenantId, String workspaceId);

    Optional<Tenant> getByCode(String tenantCode);

    Optional<Tenant> getById(String tenantId);

    Optional<Tenant> getByOwner(String owner);

    Optional<Tenant> getByWorkspace(String workspaceId);

    Optional<Tenant> getByRealm(String realmId);

    Optional<Tenant> getByWorkspaceAndRealm(Workspace workspace, Realm realm);

    Optional<Tenant> getByWorkspaceAndRealm(String workspaceId, String realmId);

    List<Tenant> getAll();

    List<Tenant> getActiveTenants();

    List<Tenant> getSuspendedTenants();

    List<Tenant> getDeletedTenants();

    boolean exists(String tenantId);

    boolean existsByWorkspaceAndRealm(String workspaceId, String realmId);
}
