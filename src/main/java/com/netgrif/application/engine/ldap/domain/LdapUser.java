package com.netgrif.application.engine.ldap.domain;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
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

    @Indexed(unique = true)
    private String dn;

    private String commonName;

    private String uid;

    private String homeDirectory;

    private Set<String> memberOf;

    public LdapUser() {
    }

    public LdapUser(ObjectId id) {
        this();
        this._id = id;
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
        return _id.toString();
    }


    @Override
    public LoggedUser transformToLoggedUser() {
        LdapLoggedUser loggedUser = new LdapLoggedUser(this.getStringId(), this.getEmail(), this.getPassword(), getDn(), getCommonName(), getMemberOf(),  getUid(), getHomeDirectory(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        if (!this.getProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getProcessRoles());
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
        this.setProcessRoles(user.getProcessRoles());
        this.setState(user.getState());
    }
}
