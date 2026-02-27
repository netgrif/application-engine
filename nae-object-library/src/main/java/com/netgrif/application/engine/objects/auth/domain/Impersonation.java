package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.querydsl.core.annotations.QueryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@QueryEntity
@NoArgsConstructor
@AllArgsConstructor
public class Impersonation implements Serializable {

    private ObjectId id;

    private String realmId;

    private String impersonatedId;
    private String impersonatedUsername;

    private List<String> impersonatorUsersIds;
    private List<String> impersonatorUsersNames;

    private List<String> impersonatorGroupsIds;
    private List<String> impersonatorGroupsNames;

    private LocalDateTime impersonatedFrom;
    private LocalDateTime impersonatedTo;

    private Set<String> impersonatedRoles;
    private Set<String> impersonatedAuthorities;

    private boolean impersonatedProcessesListAllowing;
    private List<String> impersonatedProcesses;

    public Impersonation(String realmId, String impersonatedId, String impersonatedUsername, List<String> impersonatorUsersIds,
                         List<String> impersonatorUsersNames, List<String> impersonatorGroupsIds, List<String> impersonatorGroupsNames,
                         LocalDateTime impersonatedFrom, LocalDateTime impersonatedTo, Set<String> impersonatedRoles,
                         Set<String> impersonatedAuthorities, boolean impersonatedProcessesListAllowing, List<String> impersonatedProcesses) {
        this.realmId = realmId;
        this.impersonatedId = impersonatedId;
        this.impersonatedUsername = impersonatedUsername;
        this.impersonatorUsersIds = impersonatorUsersIds;
        this.impersonatorUsersNames = impersonatorUsersNames;
        this.impersonatorGroupsIds = impersonatorGroupsIds;
        this.impersonatorGroupsNames = impersonatorGroupsNames;
        this.impersonatedFrom = impersonatedFrom;
        this.impersonatedTo = impersonatedTo;
        this.impersonatedRoles = impersonatedRoles;
        this.impersonatedAuthorities = impersonatedAuthorities;
        this.impersonatedProcessesListAllowing = impersonatedProcessesListAllowing;
        this.impersonatedProcesses = impersonatedProcesses;
    }

    public Impersonation(String realmId, String impersonatedId, String impersonatedUsername, LocalDateTime impersonatedFrom, LocalDateTime impersonatedTo) {
        this.realmId = realmId;
        this.impersonatedId = impersonatedId;
        this.impersonatedUsername = impersonatedUsername;
        this.impersonatedFrom = impersonatedFrom;
        this.impersonatedTo = impersonatedTo;
        this.impersonatorUsersIds = new ArrayList<>();
        this.impersonatorUsersNames = new ArrayList<>();
        this.impersonatorGroupsIds = new ArrayList<>();
        this.impersonatorGroupsNames = new ArrayList<>();
        this.impersonatedRoles = new HashSet<>();
        this.impersonatedAuthorities = new HashSet<>();
        this.impersonatedProcesses = new ArrayList<>();
    }

    public void addImpersonatorUserId(String impersonatorUserId) {
        if (this.impersonatorUsersIds == null) {
            this.impersonatorUsersIds = new ArrayList<>();
        }
        this.impersonatorUsersIds.add(impersonatorUserId);
    }

    public void addImpersonatorUserName(String impersonatorUsername) {
        if (this.impersonatorUsersNames == null) {
            this.impersonatorUsersNames = new ArrayList<>();
        }
        this.impersonatorUsersNames.add(impersonatorUsername);
    }

    public void addImpersonatorGroupId(String impersonatorGroupId) {
        if (this.impersonatorGroupsIds == null) {
            this.impersonatorGroupsIds = new ArrayList<>();
        }
        this.impersonatorGroupsIds.add(impersonatorGroupId);
    }

    public void addImpersonatorGroupName(String impersonatorGroupName) {
        if (this.impersonatorGroupsNames == null) {
            this.impersonatorGroupsNames = new ArrayList<>();
        }
        this.impersonatorGroupsNames.add(impersonatorGroupName);
    }

    public void addImpersonatedRole(ProcessRole role) {
        if (this.impersonatedRoles == null) {
            this.impersonatedRoles = new HashSet<>();
        }
        this.impersonatedRoles.add(role.getStringId());
    }

    public void addImpersonatedAuthority(Authority authority) {
        if (this.impersonatedAuthorities == null) {
            this.impersonatedAuthorities = new HashSet<>();
        }
        this.impersonatedAuthorities.add(authority.getStringId());
    }

    public void addImpersonatedProcess(String processId) {
        if (this.impersonatedProcesses == null) {
            this.impersonatedProcesses = new ArrayList<>();
        }
        this.impersonatedProcesses.add(processId);
    }

    @Override
    public String toString() {
        return "Impersonation{" +
                "id=" + id +
                ", realmId='" + realmId + '\'' +
                ", impersonatedId='" + impersonatedId + '\'' +
                ", impersonatedUsername='" + impersonatedUsername + '\'' +
                ", impersonatorUsersIds=" + impersonatorUsersIds +
                ", impersonatorUsersNames=" + impersonatorUsersNames +
                ", impersonatorGroupsIds=" + impersonatorGroupsIds +
                ", impersonatorGroupsNames=" + impersonatorGroupsNames +
                ", impersonatedFrom=" + impersonatedFrom +
                ", impersonatedTo=" + impersonatedTo +
                ", impersonatedRoles=" + impersonatedRoles +
                ", impersonatedAuthorities=" + impersonatedAuthorities +
                ", impersonatedProcessesListAllowing=" + impersonatedProcessesListAllowing +
                ", impersonatedProcesses=" + impersonatedProcesses +
                '}';
    }
}
