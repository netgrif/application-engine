package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LoggedUser} that integrates with Spring Security's {@link UserDetails} interface.
 * This class provides authentication and authorization details for a logged-in user within the Spring Security framework.
 * It extends {@link LoggedUser} to maintain user session information and implements {@link UserDetails} for Spring Security integration.
 *
 * @see LoggedUser
 * @see UserDetails
 * @see com.netgrif.application.engine.objects.auth.domain.AbstractUser
 */
@AllArgsConstructor
public class LoggedUserImpl extends LoggedUser implements UserDetails {

    /**
     * Constructs a new LoggedUserImpl with ObjectId as identifier.
     *
     * @param id The user's unique identifier as {@link ObjectId}
     * @param realmId The identifier of the security realm
     * @param username The user's username
     * @param firstName The user's first name
     * @param middleName The user's middle name (optional)
     * @param lastName The user's last name
     * @param email The user's email address
     * @param avatar The URL of user's avatar image
     * @param workspaceId The identifier of user's workspace
     * @param providerOrigin The authentication provider's origin
     * @param mfaMethods Set of enabled Multi-Factor Authentication methods
     * @param sessionTimeout Duration after which the user's session times out
     */
    public LoggedUserImpl(ObjectId id, String realmId, String username, String firstName, String middleName, String lastName,
                         String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods,
                         Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar, workspaceId, providerOrigin,
                mfaMethods, sessionTimeout);
    }

    /**
     * Constructs a new LoggedUserImpl with String as identifier.
     *
     * @param id The user's unique identifier as String
     * @param realmId The identifier of the security realm
     * @param username The user's username
     * @param firstName The user's first name
     * @param middleName The user's middle name (optional)
     * @param lastName The user's last name
     * @param email The user's email address
     * @param avatar The URL of user's avatar image
     * @param workspaceId The identifier of user's workspace
     * @param providerOrigin The authentication provider's origin
     * @param mfaMethods Set of enabled Multi-Factor Authentication methods
     * @param sessionTimeout Duration after which the user's session times out
     */
    public LoggedUserImpl(String id, String realmId, String username, String firstName, String middleName, String lastName,
                         String email, String avatar, String workspaceId, String providerOrigin, Set<String> mfaMethods,
                         Duration sessionTimeout) {
        super(id, realmId, username, firstName, middleName, lastName, email, avatar, workspaceId, providerOrigin,
                mfaMethods, sessionTimeout);
    }

    /**
     * Returns the authorities granted to the user. Cannot return null.
     * Transforms the user's authority set into Spring Security's {@link GrantedAuthority} objects.
     *
     * @return A collection of granted authorities, never null
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritySet().stream().map(authority -> {
            if (authority instanceof AuthorityImpl authorityImpl) {
                return authorityImpl;
            }
            return new AuthorityImpl(authority);
        }).collect(Collectors.toList());
    }

    /**
     * Returns the password used to authenticate the user.
     * In this implementation, password is not stored and "N/A" is returned.
     *
     * @return The constant string "N/A"
     */
    @Override
    public String getPassword() {
        return "N/A";
    }

    /**
     * Sets the password for the user.
     * This implementation is empty as passwords are not stored in this class.
     *
     * @param password The password to set (ignored in this implementation)
     */
    @Override
    public void setPassword(String password) {
    }
}