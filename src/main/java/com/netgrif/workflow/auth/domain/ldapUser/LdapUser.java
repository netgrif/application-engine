package com.netgrif.workflow.auth.domain.ldapUser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.auth.domain.AbstractUser;
import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "ldap_user")
public class LdapUser extends AbstractUser {

    @Id
    protected ObjectId _id;

    private String email;

    private String telNumber;

    private String avatar;

    @JsonIgnore
    private String password;

    @NotNull
    @Indexed
    private String name;

    @NotNull
    @Indexed
    private String surname;

    private String token;

    private LocalDateTime expirationDate;

    private String dn;

    private String commonName;

    private String uid;

    private String homeDirectory;

    public LdapUser() {
    }

    public LdapUser(String id) {
        this._id = new ObjectId(id);
    }

    public LdapUser(String dn, String commonName, String uid, String homeDirectory,
                    String email, String name, String surname, String telNumber) {
        this.setEmail(email);
        this.setName(name);
        this.setSurname(surname);
        this.setDn(dn);
        this.setCommonName(commonName);
        this.setUid(uid);
        this.setHomeDirectory(homeDirectory);
        this.setTelNumber(telNumber);

    }

    @Override
    public String getStringId() {
        return _id.toString();
    }

    @Override
    public String getFullName() {
        return name + " " + surname;
    }

    @Override
    public LoggedUser transformToLoggedUser() {
        LdapLoggedUser loggedUser = new LdapLoggedUser(this.getStringId(), this.getEmail(), this.getPassword(), getDn(), getCommonName(), getUid(), getHomeDirectory(), this.getAuthorities());
        loggedUser.setFullName(this.getFullName());
        if (!this.getProcessRoles().isEmpty())
            loggedUser.parseProcessRoles(this.getProcessRoles());
        loggedUser.setGroups(this.getNextGroups());

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
        this.setProcessRoles(user.getProcessRoles());
        this.setState(user.getState());
    }
}
