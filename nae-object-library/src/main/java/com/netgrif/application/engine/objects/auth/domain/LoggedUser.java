package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Represents a logged-in user in the application, extending the {@link AbstractUser} class.
 * This class maintains session-specific information and authentication details for a user
 * who has successfully logged into the system.
 *
 * @see AbstractUser
 * @see Serializable
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class LoggedUser extends AbstractUser implements Serializable {

    /**
     * The identifier of the workspace associated with the logged user.
     */
    private String workspaceId;

    /**
     * The authentication provider origin from which the user was authenticated.
     */
    private String providerOrigin;

    /**
     * Set of Multi-Factor Authentication methods enabled for this user.
     */
    private Set<String> mfaMethods;

    /**
     * The duration after which the user's session will timeout.
     * This field is marked as transient to prevent serialization.
     * Default value is 30 minutes.
     */
    private transient Duration sessionTimeout = Duration.ofMinutes(30);

    /**
     * Represents the user that the current user is impersonating.
     * This variable is used when a user temporarily assumes the identity of another
     * user within the system for authorized actions, such as administrative/business tasks.
     */
    private LoggedUser impersonatedUser;

    private boolean impersonatedProcessesListAllowing;

    private List<String> impersonatedProcesses;


    /**
     * Constructs a new LoggedUser instance with all attributes.
     *
     * @param id             The unique identifier as {@link ObjectId}
     * @param realmId        The identifier of the security realm
     * @param username       The user's username
     * @param firstName      The user's first name
     * @param middleName     The user's middle name
     * @param lastName       The user's last name
     * @param email          The user's email address
     * @param avatar         The user's avatar URL
     * @param workspaceId    The identifier of user's workspace
     * @param providerOrigin The authentication provider origin
     * @param mfaMethods     The set of enabled MFA methods
     * @param sessionTimeout The duration of session timeout
     */
    public LoggedUser(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar);
        this.workspaceId = workspaceId;
        this.providerOrigin = providerOrigin;
        this.mfaMethods = mfaMethods;
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Constructs a new LoggedUser instance with String id and all other attributes.
     *
     * @param id             The unique identifier as String
     * @param realmId        The identifier of the security realm
     * @param username       The user's username
     * @param firstName      The user's first name
     * @param middleName     The user's middle name
     * @param lastName       The user's last name
     * @param email          The user's email address
     * @param avatar         The user's avatar URL
     * @param workspaceId    The identifier of user's workspace
     * @param providerOrigin The authentication provider origin
     * @param mfaMethods     The set of enabled MFA methods
     * @param sessionTimeout The duration of session timeout
     */
    public LoggedUser(String id, String realmId, String username, String firstName, String middleName, String lastName, String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods, Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar);
        this.workspaceId = workspaceId;
        this.providerOrigin = providerOrigin;
        this.mfaMethods = mfaMethods;
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public boolean isImpersonating() {
        return this.impersonatedUser != null;
    }

    public LoggedUser getSelfOrImpersonated() {
        return this.isImpersonating() ? this.impersonatedUser : this;
    }

    @Override
    public String getSelfOrImpersonatedStringId() {
        return this.isImpersonating() ? this.impersonatedUser.getStringId() : this.getStringId();
    }

    public boolean isProcessAccessDeny() {
        return !this.isAdmin() && this.isImpersonating() && this.isImpersonatedProcessesListAllowing()
                && (this.getImpersonatedProcesses() == null || this.getImpersonatedProcesses().isEmpty());
    }

    @Override
    public Set<ProcessRole> getProcessRoles() {
        if (!this.isImpersonating()) {
            return super.getProcessRoles();
        }
        return this.impersonatedUser.getProcessRoles();
    }

    public void setProcessRolesToLoggedUser(Set<ProcessRole> processRoleSet) {
        super.setProcessRoles(processRoleSet);
    }

    @Override
    public void setProcessRoles(Set<ProcessRole> processRoleSet) {
        if (!this.isImpersonating()) {
            super.setProcessRoles(processRoleSet);
            return;
        }
        this.impersonatedUser.setProcessRoles(processRoleSet);
    }

    @Override
    public Set<Authority> getAuthoritySet() {
        if (!this.isImpersonating()) {
            return super.getAuthoritySet();
        }
        return this.impersonatedUser.getAuthoritySet();
    }

    @Override
    public void setAuthoritySet(Set<Authority> authoritySet) {
        if (!this.isImpersonating()) {
            super.setAuthoritySet(authoritySet);
            return;
        }
        this.impersonatedUser.setAuthoritySet(authoritySet);
    }

    @Override
    public boolean isAdmin() {
        if (!this.isImpersonating()) {
            return super.isAdmin();
        }
        return this.impersonatedUser.isAdmin();
    }

    public boolean hasProcessAccess(String processIdentifier) {
        if (!this.isImpersonating()) {
            return true;
        }
        if (this.impersonatedProcesses == null || this.impersonatedProcesses.isEmpty()) {
            return !this.impersonatedProcessesListAllowing;
        }
        return (this.impersonatedProcessesListAllowing && this.impersonatedProcesses.contains(processIdentifier))
                || (!this.impersonatedProcessesListAllowing && !this.impersonatedProcesses.contains(processIdentifier));
    }
}