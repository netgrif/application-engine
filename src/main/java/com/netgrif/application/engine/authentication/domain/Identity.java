package com.netgrif.application.engine.authentication.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
// todo 2058 class simplify class declaration (user)
public class Identity extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 3031325636490953409L;

    @Id
    protected String id;
    protected String password;
    protected String mainActorId;
    protected Set<String> additionalActorIds;
    // todo: release/8.0.0 store IP address in redis?

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