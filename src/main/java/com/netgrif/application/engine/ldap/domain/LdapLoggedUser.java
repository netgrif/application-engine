package com.netgrif.application.engine.ldap.domain;


import com.netgrif.application.engine.auth.domain.*;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapLoggedUser extends LoggedUser {

    @Getter
    @Setter
    private String dn;

    @Getter
    @Setter
    private String commonName;

    @Getter
    @Setter
    private String uid;

    @Getter
    @Setter
    private Set<String> memberOf;

    @Getter
    @Setter
    private String homeDirectory;


    public LdapLoggedUser(String id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(id, username, password, authorities);
    }


    public LdapLoggedUser(String id, String username, String password, String dn, String commonName, Set<String> memberOf, String uid, String homeDirectory, Collection<? extends GrantedAuthority> authorities) {
        super(id, username, password, authorities);
        this.dn = dn;
        this.commonName = commonName;
        this.memberOf = memberOf;
        this.uid = uid;
        this.homeDirectory = homeDirectory;
    }

    public IUser transformToUser() {
        LdapUser user = new LdapUser(new ObjectId(this.id));
        user.setEmail(getUsername());
        String[] names = this.getFullName().split(" ");
        user.setName(names[0]);
        user.setSurname(names[1]);
        user.setDn(this.dn);
        user.setCommonName(this.commonName);
        user.setUid(this.uid);
        user.setMemberOf(this.memberOf);
        user.setHomeDirectory(homeDirectory);
        user.setState(UserState.ACTIVE);
        user.setPassword("n/a");
        user.setAuthorities(getAuthorities().stream().map(a -> ((Authority) a)).collect(Collectors.toSet()));
        user.setProcessRoles(this.getProcessRoles().stream().map(roleId -> {
            ProcessRole role = new ProcessRole();
            role.set_id(roleId);
            return role;
        }).collect(Collectors.toSet()));
        if (this.isImpersonating()) {
            user.setImpersonated(this.getImpersonated().transformToUser());
        }
        return user;
    }

}
