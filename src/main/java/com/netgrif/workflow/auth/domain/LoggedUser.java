package com.netgrif.workflow.auth.domain;

import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class LoggedUser extends org.springframework.security.core.userdetails.User {

    public static final long serialVersionUID = 3031325636490953409L;

    @Getter
    @Setter
    protected String id;

    @Getter
    @Setter
    protected String fullName;

    @Getter
    @Setter
    protected Set<String> groups;

    @Getter
    @Setter
    protected Set<String> processRoles;

    @Getter
    @Setter
    protected boolean anonymous;

    public LoggedUser(String id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.processRoles = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public void parseProcessRoles(Set<ProcessRole> processRoles) {
        processRoles.forEach(role -> this.processRoles.add(role.getStringId()));
    }

    public boolean isAdmin() {
        return getAuthorities().contains(new Authority(Authority.admin));
    }

    public String getEmail() {
        return getUsername();
    }

    public IUser transformToUser() {
        User user = new User(new ObjectId(this.id));
        user.setEmail(getUsername());
        String[] names = this.fullName.split(" ");
        user.setName(names[0]);
        user.setSurname(names[1]);
        user.setPassword(getPassword());
        user.setState(UserState.ACTIVE);
        user.setAuthorities(getAuthorities().stream().map(a -> ((Authority) a)).collect(Collectors.toSet()));
        user.setNextGroups(groups.stream().map(String::new).collect(Collectors.toSet()));
        user.setProcessRoles(processRoles.stream().map(roleId -> {
            ProcessRole role = new ProcessRole();
            role.set_id(roleId);
            return role;
        }).collect(Collectors.toSet()));

        return user;
    }

    public AnonymousUser transformToAnonymousUser() {
        AnonymousUser anonym = new AnonymousUser(new ObjectId(this.id));
        anonym.setEmail(getUsername());
        anonym.setName("Anonymous");
        anonym.setSurname("User");
        anonym.setPassword(null);
        anonym.setState(UserState.ACTIVE);
        anonym.setAuthorities(getAuthorities().stream().map(a -> ((Authority) a)).collect(Collectors.toSet()));
        anonym.setNextGroups(groups.stream().map(String::new).collect(Collectors.toSet()));
        anonym.setProcessRoles(processRoles.stream().map(roleId -> {
            ProcessRole role = new ProcessRole();
            role.set_id(roleId);
            return role;
        }).collect(Collectors.toSet()));
        return anonym;
    }

    @Override
    public String toString() {
        return "LoggedUser{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", groups=" + groups +
                ", processRoles=" + processRoles +
                '}';
    }

    public Author transformToAuthor() {
        Author author = new Author();
        author.setId(this.id);
        author.setEmail(getUsername());
        author.setFullName(this.fullName);

        return author;
    }
}