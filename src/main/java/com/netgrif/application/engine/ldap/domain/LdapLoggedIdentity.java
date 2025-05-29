package com.netgrif.application.engine.ldap.domain;


import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.Set;

@Setter
@Getter
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapLoggedIdentity extends LoggedIdentity {

    private String dn;

    private String commonName;

    private String uid;

    private Set<String> memberOf;

    private String homeDirectory;


    public LdapLoggedIdentity(String id, String username, String password, String dn, String commonName,
                              Set<String> memberOf, String uid, String homeDirectory) {
        super(null, id, null, username, password, null);
        this.dn = dn;
        this.commonName = commonName;
        this.memberOf = memberOf;
        this.uid = uid;
        this.homeDirectory = homeDirectory;
    }

//    public IUser transformToUser() {
//        LdapUser user = new LdapUser(new ObjectId(this.id));
//        user.setEmail(getUsername());
//        String[] names = this.getFullName().split(" ");
//        user.setName(names[0]);
//        user.setSurname(names[1]);
//        user.setDn(this.dn);
//        user.setCommonName(this.commonName);
//        user.setUid(this.uid);
//        user.setMemberOf(this.memberOf);
//        user.setHomeDirectory(homeDirectory);
//        user.setState(IdentityState.ACTIVE);
//        user.setPassword("n/a");
//        user.setAuthorities(getAuthorities().stream().map(a -> ((SessionRole) a)).collect(Collectors.toSet()));
////        user.setRoles(this.getRoles().stream().map(roleId -> {
////            Role role = new Role();
////            role.setStringId(roleId);
////            return role;
////        }).collect(Collectors.toSet()));
//        if (this.isImpersonating()) {
//            user.setImpersonated(this.getImpersonated().transformToUser());
//        }
//        return user;
//    }

}
