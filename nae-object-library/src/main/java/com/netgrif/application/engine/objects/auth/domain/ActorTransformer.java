package com.netgrif.application.engine.objects.auth.domain;

import lombok.Setter;

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
     * The lambda expression provides a no-op factory that throws an exception to indicate
     * that no valid LoggedUserFactory has been set.
     * <p>
     * -- SETTER --
     * Sets the factory used for creating LoggedUser instances.
     *
     * @param f the LoggedUserFactory implementation to be used
     */
    @Setter
    private static LoggedUserFactory loggedUserFactory = () -> {
        throw new IllegalStateException("No LoggedUserFactory configured");
    };


    /**
     * Factory for creating new AbstractUser instances.
     * Default implementation throws IllegalStateException if no factory is configured.
     * The lambda expression provides a no-op factory that throws an exception to indicate
     * that no valid UserFactory has been set.
     * <p>
     * -- SETTER --
     * Sets the factory used for creating AbstractUser instances.
     **/
    @Setter
    private static UserFactory userFactory = () -> {
        throw new IllegalStateException("No UserFactory configured");
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

    @FunctionalInterface
    public interface UserFactory {
        /**
         * Creates a new AbstractUser instance.
         * @return newly created AbstractUser instance
         */
        AbstractUser create();


        /**
         * Creates a new AbstractUser instance based on the provided LoggedUser instance.
         * By default, this method throws an IllegalStateException, and must be explicitly
         * implemented by the concrete UserFactory implementation.
         *
         * @param loggedUser the LoggedUser containing user details
         * @return newly created AbstractUser instance representing the same user
         * @throws IllegalStateException if the method is not implemented
         */
        default AbstractUser create(LoggedUser loggedUser) {
            throw new IllegalStateException("Method is not implemented");
        }
    }

    /**
     * Transforms an AbstractUser into a LoggedUser by copying all relevant user information.
     * @param user the AbstractUser to transform
     * @return new LoggedUser instance with copied user data
     */
    public static LoggedUser toLoggedUser(AbstractUser user) {
        LoggedUser loggedUser = loggedUserFactory.create();
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
        return loggedUser;
    }

    /**
     * Converts a LoggedUser into an AbstractUser by copying all relevant user information.
     *
     * @param loggedUser the LoggedUser to transform
     * @return a new AbstractUser instance with copied user data
     */
    public static AbstractUser toUser(LoggedUser loggedUser) {
        AbstractUser user = userFactory.create(loggedUser);
        user.setId(loggedUser.getStringId());
        user.setRealmId(loggedUser.getRealmId());
        user.setUsername(loggedUser.getUsername());
        user.setEmail(loggedUser.getEmail());
        user.setFirstName(loggedUser.getFirstName());
        user.setMiddleName(loggedUser.getMiddleName());
        user.setLastName(loggedUser.getLastName());
        user.setAuthoritySet(loggedUser.getAuthoritySet());
        user.setProcessRoles(loggedUser.getProcessRoles());
        user.setAttributes(loggedUser.getAttributes());
        user.setGroupIds(loggedUser.getGroupIds());
        return user;
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
        loggedUser.setWorkspaceId(workspaceId);
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