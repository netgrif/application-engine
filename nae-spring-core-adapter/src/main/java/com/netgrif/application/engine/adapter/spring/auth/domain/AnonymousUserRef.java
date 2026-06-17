package com.netgrif.application.engine.adapter.spring.auth.domain;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an anonymous user reference in the application.
 * This class provides functionality for managing anonymous user sessions with associated roles,
 * authorities, and groups.
 */
@Data
@Document(collection = "anonym_user")
public class AnonymousUserRef {

    /**
     * Unique identifier for the anonymous user.
     */
    @Id
    private String id;

    /**
     * Unique identifier for the security realm this anonymous user belongs to.
     */
    @Indexed(unique = true)
    private String realmId;

    /**
     * Timestamp when this anonymous user was created.
     */
    private LocalDateTime createdAt;

    /**
     * Display name for the anonymous user. Defaults to "Anonymous".
     */
    private String displayName = "Anonymous";

    /**
     * Current state of the anonymous user account.
     * @see UserState
     */
    @NotNull
    private UserState state = UserState.ACTIVE;

    /**
     * Set of authorities granted to this anonymous user.
     * @see Authority
     */
    private Set<Authority> authorities = new HashSet<>();

    /**
     * Set of process roles assigned to this anonymous user.
     * @see ProcessRole
     */
    private Set<ProcessRole> processRoles = new HashSet<>();

    /**
     * Set of group identifiers this anonymous user belongs to.
     */
    private Set<String> groupIds = new HashSet<>();

    /**
     * Duration after which the anonymous user session times out.
     * Default value is 30 minutes.
     */
    private transient Duration sessionTimeout = Duration.ofMinutes(30);

    /**
     * Set of groups this anonymous user belongs to.
     * This field is not persisted in the database.
     * @see Group
     */
    @BsonIgnore
    private Set<Group> groups = new HashSet<>();

    /**
     * Creates a new anonymous user reference with current timestamp.
     */
    public AnonymousUserRef() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new anonymous user reference with specified realm ID and current timestamp.
     * 
     * @param realmId the ID of the security realm this anonymous user belongs to
     */
    public AnonymousUserRef(String realmId) {
        this.realmId = realmId;
        this.createdAt = LocalDateTime.now();
    }
}