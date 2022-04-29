package com.netgrif.application.engine.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Document
public class Authority implements GrantedAuthority {

    public static final long serialVersionUID = 2839744057647464485L;

//    public static final String PERMISSION = "PERM_";
//    public static final String ROLE = "ROLE_";
//
//    public static final String admin = ROLE + "ADMIN";
//    public static final String systemAdmin = ROLE + "SYSTEMADMIN";
//    public static final String user = ROLE + "USER";
//    public static final String anonymous = ROLE + "ANONYMOUS";

    /**
     * Domain constants
     * */
    public static final String ADMIN = "*";
    public static final String PROCESS = "PROCESS";
    public static final String FILTER = "FILTER";
    public static final String USER = "USER";
    public static final String GROUP = "GROUP";
    public static final String ROLE = "ROLE";
    public static final String AUTHORITY = "AUTHORITY";

//    /**
//     * Authority objects constants
//     * */
//    public static final String processUpload = "PROCESS.UPLOAD";
//    public static final String processDelete = "PROCESS.DELETE";
//    public static final String filterUpload = "FILTER.UPLOAD";
//    public static final String filterDelete = "FILTER.DELETE";
//    public static final String userCreate = "USER.CREATE";
//    public static final String userDelete = "USER.DELETE";
//    public static final String userEdit = "USER.EDIT";
//    public static final String groupCreate = "GROUP.CREATE";
//    public static final String groupDelete = "GROUP.DELETE";
//    public static final String groupAddUser = "GROUP.ADD_USER";
//    public static final String groupRemoveUser = "GROUP.REMOVE_USER";
//    public static final String roleCreate = "ROLE.CREATE";
//    public static final String roleDelete = "ROLE.DELETE";
//    public static final String authorityCreate = "AUTHORITY.CREATE";
//    public static final String authorityDelete = "AUTHORITY.DELETE";

    public static final List<AuthorityEnum> allAuthorities = new ArrayList<>(List.of(AuthorityEnum.values()));

    @Id
    @Getter
    private ObjectId _id;

    @NotNull
    @JsonIgnore
    @Getter
    @Setter
    private String name;

    @JsonIgnore
    @Getter
    @Setter
    private Set<String> users;

    public Authority() {
    }

    public Authority(String name) {
        this.name = name;
    }

    public void addUser(IUser user) {
        users.add(user.getStringId());
    }

    public String getStringId() {
        return _id.toString();
    }

    @Override
    public String getAuthority() {
        return this.name;
    }

    public void setAuthority(String authority) {
        this.name = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authority authority = (Authority) o;

        return name.equals(authority.name);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + _id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
