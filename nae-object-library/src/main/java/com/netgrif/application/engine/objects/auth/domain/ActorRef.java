package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents a reference to an actor (user) in the system.
 * This class implements {@link Serializable} to support object serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorRef implements Serializable {

    /**
     * The unique identifier of the actor.
     * This ID is typically used as a primary key in the database.
     */
    private String id;

    /**
     * The identifier of the realm to which this actor belongs.
     * Used for authentication and authorization purposes.
     */
    private String realmId;

    /**
     * The unique identifier string for the actor, typically used as a username.
     * This field is used for authentication and user identification purposes.
     */
    private String identifier;

    /**
     * The display name of the actor that is shown in the user interface.
     * This can be different from the identifier and typically includes the full name.
     */
    private String displayName;

    /**
     * The set of authorities (permissions/roles) granted to this actor.
     * Authorities define what actions the actor is allowed to perform in the system.
     * This set typically includes both system-level authorities and process-specific roles.
     *
     * @see Authority
     */
    private Set<String> authorities;

    /**
     * Gets the username of the actor.
     * This is an alias for {@link #identifier}.
     *
     * @return the actor's username (identifier)
     */
    public String getUsername() {
        return identifier;
    }

    /**
     * Gets the full name of the actor.
     * This is an alias for {@link #displayName}.
     *
     * @return the actor's full display name
     */
    public String getFullName() {
        return displayName;
    }

    /**
     * Determines whether this actor represents an anonymous user.
     * An actor is considered anonymous if it has at least one authority with the name
     * {@link Authority#anonymous}.
     * <p>
     * This method is typically used to check access permissions and determine whether
     * the user needs to authenticate before performing certain operations. Anonymous users
     * usually have limited access rights compared to authenticated users.
     * </p>
     *
     * @return {@code true} if the actor has the anonymous authority, {@code false} otherwise.
     * Returns {@code false} if authorities is {@code null}.
     * @see Authority#anonymous
     */
    public boolean isAnonymous() {
        return authorities != null && authorities.stream().allMatch(it -> it.equals(Authority.anonymous));
    }
}