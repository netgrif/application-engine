package com.netgrif.workflow.oauth.domain;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public class OAuthLoggedUser extends LoggedUser {

    @Getter
    protected String dbId;

    public OAuthLoggedUser(String oauthId, String dbId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(oauthId, username, "n/a", authorities);
        this.dbId = dbId;
    }

    public IUser transformToUser() {
        OAuthUser user = new OAuthUser(new ObjectId(this.dbId));
        user.setEmail(getUsername());
        String[] names = this.fullName.split(" ");
        user.setName(names[0]);
        user.setSurname(names[1]);
        user.setState(UserState.ACTIVE);
        user.setAuthorities(getAuthorities().stream().map(a -> ((Authority) a)).collect(Collectors.toSet()));
        user.setGroups(groups.stream().map(Group::new).collect(Collectors.toSet()));
        user.setProcessRoles(processRoles.stream().map(roleId -> {
            ProcessRole role = new ProcessRole();
            role.set_id(roleId);
            return role;
        }).collect(Collectors.toSet()));

        return user;
    }

}
