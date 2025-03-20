package com.netgrif.application.engine.ldap.domain;

import com.netgrif.application.engine.authentication.domain.Identity;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;


@Data
@Document(collection = "user")
@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapUser extends User {

    @Indexed
    private String dn;

    private String commonName;

    private String uid;

    private String homeDirectory;

    private Set<String> memberOf;

    public LdapUser() {
    }

    public LdapUser(ObjectId id) {
        this();
        this.id = id;
        nextGroups = new HashSet<>();
    }

    public LdapUser(String dn, String commonName, String uid, String homeDirectory,
                    String email, String name, String surname, Set<String> memberOf, String telNumber) {
        this.setEmail(email);
        this.setName(name);
        this.setSurname(surname);
        this.setDn(dn);
        this.setCommonName(commonName);
        this.setUid(uid);
        this.setHomeDirectory(homeDirectory);
        this.setMemberOf(memberOf);
        this.setTelNumber(telNumber);

    }

    @Override
    public String getStringId() {
        return id.toString();
    }


    @Override
    public Identity transformToLoggedUser() {
        LdapIdentity loggedUser = new LdapIdentity(this.getStringId(), this.getEmail(), this.getPassword(), getDn(), getCommonName(), getMemberOf(), getUid(), getHomeDirectory(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        // todo 2058
//        if (!this.getRoles().isEmpty())
//            loggedUser.parseRoles(this.getRoles());
        loggedUser.setGroups(this.getNextGroups());
        if (this.isImpersonating()) {
            loggedUser.impersonate(this.getImpersonated().transformToLoggedUser());
        }

        return loggedUser;
    }

    public void loadFromUser(IUser user) {
        this.setEmail(user.getEmail());
        this.setName(user.getName());
        this.setSurname(user.getSurname());
        this.setAvatar(user.getAvatar());
        this.setTelNumber(user.getTelNumber());
        this.setNextGroups(user.getNextGroups());
        // todo 2058
//        this.setRoles(user.getRoles());
        this.setState(user.getState());
    }
}
