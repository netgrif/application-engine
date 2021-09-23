package com.netgrif.workflow.ldap.domain;


import com.netgrif.workflow.auth.domain.User;
import lombok.Data;

import javax.persistence.Entity;


@Data
@Entity
public class LdapUser extends User {

    protected String dn;

    protected String commonName;

    protected String uid;

    protected String homeDirectory;

    public LdapUser() {
    }


    public LdapUser(Long id) {
        super();
        this.id = id;
    }


    public LdapUser(String dn, String commonName, String uid, String homeDirectory,
                    String email, String password, String name, String surname, String telNumber) {
        super(email, password, name, surname);
        this.setDn(dn);
        this.setTelNumber(telNumber);
        this.setCommonName(commonName);
        this.setUid(uid);
        this.setHomeDirectory(homeDirectory);
    }


    public LdapUser(String email, String password, String name, String surname) {
        super(email, password, name, surname);
    }


    public void loadFromUser(User user) {
        this.setEmail(user.getEmail());
        this.setPassword(user.getPassword());
        this.setName(user.getName());
        this.setSurname(user.getSurname());
        this.setAvatar(user.getAvatar());
        this.setTelNumber(user.getTelNumber());
        this.setToken(user.getToken());
        this.setNextGroups(user.getNextGroups());
        this.setProcessRoles(user.getProcessRoles());
        this.setState(user.getState());
    }

}