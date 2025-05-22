package com.netgrif.application.engine.objects.auth.domain;

import java.time.Duration;

public class ActorTransformer {

    private static LoggedUserFactory factory = () -> {
        throw new IllegalStateException("No LoggedUserFactory configured");
    };

    @FunctionalInterface
    public interface LoggedUserFactory {
        LoggedUser create();
    }

    public static void setLoggedUserFactory(LoggedUserFactory f) {
        factory = f;
    }

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
        return loggedUser;
    }

    public static LoggedUser toLoggedUser(AbstractUser user, String workspaceId, String providerOrigin, Duration sessionTimeout) {
        LoggedUser loggedUser = toLoggedUser(user);
        loggedUser.setWorkspaceId(workspaceId);
        loggedUser.setProviderOrigin(providerOrigin);
        loggedUser.setSessionTimeout(sessionTimeout);
        return loggedUser;
    }

    public static ActorRef toActorRef(AbstractUser user) {
        return new ActorRef(
                user.getStringId(),
                user.getRealmId(),
                user.getUsername(),
                user.getName()
        );
    }

    public static ActorRef toActorRef(LoggedUser loggedUser) {
        return new ActorRef(
                loggedUser.getStringId(),
                loggedUser.getRealmId(),
                loggedUser.getUsername(),
                loggedUser.getName()
        );
    }

    public static ActorRef toActorRef(Group group) {
        return new ActorRef(
                group.getStringId(),
                group.getRealmId(),
                group.getIdentifier(),
                group.getName()
        );
    }

    public static ActorRef anonymizedActorRef() {
        return new ActorRef(
                "",
                "",
                "***",
                "***"
        );
    }
}
