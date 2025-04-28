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

@Data
@Document(collection = "anonym_user")
public class AnonymousUserRef {

    @Id
    private String id;

    @Indexed(unique = true)
    private String realmId;

    private LocalDateTime createdAt;

    private String displayName = "Anonymous";

    @NotNull
    private UserState state = UserState.ACTIVE;

    private Set<Authority> authorities = new HashSet<>();

    private Set<ProcessRole> processRoles = new HashSet<>();

    private Set<String> groupIds = new HashSet<>();

    private transient Duration sessionTimeout = Duration.ofMinutes(30);

    @BsonIgnore
    private Set<Group> groups = new HashSet<>();

    public AnonymousUserRef() {
        this.createdAt = LocalDateTime.now();
    }

    public AnonymousUserRef(String realmId) {
        this.realmId = realmId;
        this.createdAt = LocalDateTime.now();
    }
}
