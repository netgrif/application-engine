package com.netgrif.application.engine.objects.tenant;


import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public abstract class Tenant implements Serializable {

    public record RealmRef(String name, boolean defaultRealm) {

    }

    public enum TenantStatus {
        ACTIVE, INACTIVE, SUSPENDED, DELETED
    }

    @Serial
    private static final long serialVersionUID = 26983148877935L;

    private final String id;

    @Indexed
    private final String tenantCode;

    private TenantStatus status = TenantStatus.INACTIVE;

    @Setter
    private String owner;

    @Setter
    private List<String> workspaces;

    @Setter
    private List<RealmRef> realms;

    @Setter
    private String name;

    public Tenant(String id, String tenantCode) {
        this.id = id;
        this.tenantCode = tenantCode;
        this.workspaces = new ArrayList<>();
        this.realms = new ArrayList<>();
    }

    public void setActive() {
        this.status = TenantStatus.ACTIVE;
    }

    public void setInactive() {
        this.status = TenantStatus.INACTIVE;
    }

    public void setSuspended() {
        this.status = TenantStatus.SUSPENDED;
    }

    public void setDeleted() {
        this.status = TenantStatus.DELETED;
    }

    public boolean isActive() {
        return TenantStatus.ACTIVE.equals(status);
    }

    public boolean isInactive() {
        return TenantStatus.INACTIVE.equals(status);
    }

    public boolean isSuspended() {
        return TenantStatus.SUSPENDED.equals(status);
    }

    public boolean isDeleted() {
        return TenantStatus.DELETED.equals(status);
    }

    public void addRealm(Realm realm) {
        realms.add(new RealmRef(realm.getName(), realm.isDefaultRealm()));
    }

    public void addWorkspace(String workspaceId) {
        workspaces.add(workspaceId);
    }

    public void removeWorkspace(String workspaceId) {
        workspaces.remove(workspaceId);
    }

    public void removeRealm(String realmId) {
        realms.stream().filter(realmRef -> realmRef.name.equals(realmId)).findFirst().ifPresent(realmRef -> realms.remove(realmRef));
    }

    public Optional<String> getDefaultRealmId() {
        return realms.stream().filter(realmRef -> realmRef.defaultRealm).findFirst().map(realmRef -> realmRef.name);
    }

}
