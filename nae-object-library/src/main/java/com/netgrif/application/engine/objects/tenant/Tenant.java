package com.netgrif.application.engine.objects.tenant;


import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
public abstract class Tenant implements Serializable {
    @Serial
    private static final long serialVersionUID = 26983148877935L ;

    private final String id;

    @Indexed
    private final String tenantCode;

    private TenantStatus status = TenantStatus.INACTIVE;

    @Setter
    private ActorRef owner;

    @Setter
    private List<String> workspaces;

    @Setter
    private List<String> realms;

    @Setter
    private String name;

    public Tenant(String id, String tenantCode) {
        this.id = id;
        this.tenantCode = tenantCode;
    }

    public Tenant(String id, String tenantCode, List<String> workspaces, List<String> realms) {
        this.id = id;
        this.tenantCode = tenantCode;
        this.workspaces = workspaces;
        this.realms = realms;
    }

    public String getOwnerRealmId() {
        return owner.getRealmId();
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

    public void addWorkspace(String workspaceId) {
        workspaces.add(workspaceId);
    }

    public void addRealm(String realmId) {
        realms.add(realmId);
    }

    public enum TenantStatus {
        ACTIVE,INACTIVE, SUSPENDED, DELETED
    }


}
