package com.netgrif.application.engine.authentication.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
// todo 2058 class simplify class declaration (user)
public class Identity extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 3031325636490953409L;

    protected String id;
    protected String mainActorId;
    protected Set<String> additionalActorIds;

    public Identity(String id, String mainActorId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this(id, mainActorId, new HashSet<>(), username, password, authorities);
    }

    public Identity(String id, String mainActorId, Set<String> additionalActorIds, String username, String password,
                    Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.mainActorId = mainActorId;
        this.additionalActorIds = additionalActorIds;
    }

    /**
     * todo javadoc
     * */
    public void addActorId(String actorId) {
        this.additionalActorIds.add(actorId);
    }

    /**
     * todo javadoc
     * */
    public void removeActorId(String actorId) {
        this.additionalActorIds.remove(actorId);
    }

    public boolean isAdmin() {
        // todo 2058 ApplicationRole
        return getAuthorities().contains(new Authority(Authority.admin));
    }

    /**
     * todo javadoc
     * */
    public String getEmail() {
        return getUsername();
    }

    @Override
    public String toString() {
        return "LoggedUser{" +
                "id=" + id +
                ", mainActorId='" + mainActorId + '\'' +
                ", additionalActorIds=" + additionalActorIds +
                '}';
    }
}