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
//
//    protected String homeDirectory;
//
//    protected String homeDirectory;
//
//    protected String homeDirectory;
//
//    protected String homeDirectory;
//
//    protected String homeDirectory;




    public LdapUser() {
    }


    public LdapUser(Long id) {
        super();
        this.id = id;
    }


    public LdapUser(String dn, String commonName, String uid, String homeDirectory,
                    String email, String password, String name, String surname) {
        super(email, password, name, surname);
        this.dn = dn;
        this.commonName = commonName;
        this.uid = uid;
        this.homeDirectory = homeDirectory;
    }


    public LdapUser(String email, String password, String name, String surname) {
        super(email, password, name, surname);
    }




//    @Override
//    public LoggedUser transformToLoggedUser() {
//        LdapLoggedUser loggedUser = new LdapLoggedUser(this.getId(), this.getEmail(), this.getPassword(), getDn(), getCommonName(), getUid(), getHomeDirectory(), this.getAuthorities());
//        loggedUser.setFullName(this.getFullName());
//        //TODO: JOZIKEEE
////        if (!this.getProcessRoles().isEmpty())
////            loggedUser.parseProcessRoles(this.getProcessRoles());
////        loggedUser.setGroups(this.getNextGroups());
//        return loggedUser;
//    }


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