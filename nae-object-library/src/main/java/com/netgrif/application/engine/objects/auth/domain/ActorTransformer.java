package com.netgrif.application.engine.objects.auth.domain;

import java.time.Duration;

/**
 * Transformer class responsible for converting between different user/actor representations
 * in the authentication system. Provides utility methods for transforming between AbstractUser,
 * LoggedUser, ActorRef and Group objects.
 */
public class ActorTransformer {

    /**
     * Factory for creating new LoggedUser instances.
     * Default implementation throws IllegalStateException if no factory is configured.
     */
    private static LoggedUserFactory factory = () -> {
        throw new IllegalStateException("No LoggedUserFactory configured");
    };

    /**
     * Functional interface defining a factory method for creating new LoggedUser instances.
     */
    @FunctionalInterface
    public interface LoggedUserFactory {
        /**
         * Creates a new LoggedUser instance.
         * @return newly created LoggedUser instance
         */
        LoggedUser create();
    }

    /**
     * Sets the factory used for creating LoggedUser instances.
     * @param f the LoggedUserFactory implementation to be used
     */
    public static void setLoggedUserFactory(LoggedUserFactory f) {
        factory = f;
    }

    /**
     * Transforms an AbstractUser into a LoggedUser by copying all relevant user information.
     * @param user the AbstractUser to transform
     * @return new LoggedUser instance with copied user data
     */
    public static LoggedUser toLoggedUser(AbstractUser user) {
        LoggedUser loggedUser = factory.create();
        loggedUser.setId(user.getStringId());
        loggedUser.setRealmId(user.getRealmId());
        loggedUser.setUsername(user.getUsername());
        loggedUser.setEmail(user.getEmail());
        loggedUser.setFirstName(user.getFirstName());
        loggedUser.setMiddleName(user.getMiddleName());
        loggedUser.setLastName(user.getLastName());
        loggedUser.setAuthoritySet(user.getAuthoritySet());
        loggedUser.setProcessRoles(user.getProcessRoles());
        loggedUser.setAttributes(user.getAttributes());
        loggedUser.setGroupIds(user.getGroupIds());
        loggedUser.setActiveWorkspaceId(user.getActiveWorkspaceId());
        loggedUser.setWorkspacePermissions(user.getWorkspacePermissions());
        return loggedUser;
    }

    /**
     * Transforms an AbstractUser into a LoggedUser with additional session-related information.
     * @param user the AbstractUser to transform
     * @param workspaceId ID of the workspace for the session
     * @param providerOrigin authentication provider origin
     * @param sessionTimeout duration after which the session times out
     * @return new LoggedUser instance with copied user data and session information
     */
    public static LoggedUser toLoggedUser(AbstractUser user, String workspaceId, String providerOrigin, Duration sessionTimeout) {
        LoggedUser loggedUser = toLoggedUser(user);
        loggedUser.setActiveWorkspaceId(workspaceId);
        loggedUser.setProviderOrigin(providerOrigin);
        loggedUser.setSessionTimeout(sessionTimeout);
        return loggedUser;
    }

    /**
     * Creates an ActorRef from an AbstractUser with basic user information.
     * @param user the AbstractUser to transform
     * @return new ActorRef containing basic user information
     */
    public static ActorRef toActorRef(AbstractUser user) {
        return new ActorRef(
                user.getStringId(),
                user.getRealmId(),
                user.getUsername(),
                user.getName()
        );
    }

    /**
     * Creates an ActorRef from a LoggedUser with basic user information.
     * @param loggedUser the LoggedUser to transform
     * @return new ActorRef containing basic user information
     */
    public static ActorRef toActorRef(LoggedUser loggedUser) {
        return new ActorRef(
                loggedUser.getStringId(),
                loggedUser.getRealmId(),
                loggedUser.getUsername(),
                loggedUser.getName()
        );
    }

    /**
     * Creates an ActorRef from a Group with basic group information.
     * @param group the Group to transform
     * @return new ActorRef containing basic group information
     */
    public static ActorRef toActorRef(Group group) {
        return new ActorRef(
                group.getStringId(),
                group.getRealmId(),
                group.getIdentifier(),
                group.getName()
        );
    }

    /**
     * Creates an anonymized ActorRef with empty identifiers and masked username/name.
     * @return new ActorRef with anonymized data
     */
    public static ActorRef anonymizedActorRef() {
        return new ActorRef(
                "",
                "",
                "***",
                "***"
        );
    }
}