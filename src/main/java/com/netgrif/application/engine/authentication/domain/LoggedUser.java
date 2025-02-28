package com.netgrif.application.engine.authentication.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoggedUser extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 3031325636490953409L;

    @Setter
    protected String id;

    @Setter
    protected String fullName;

    @Setter
    protected Set<String> groups;

    protected Set<String> roleAssignments;

    protected Set<String> roles;

    @Setter
    protected boolean anonymous;

    private LoggedUser impersonated;

    public LoggedUser(String id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
        this.roleAssignments = new HashSet<>();
        this.roles = new HashSet<>();
        this.groups = new HashSet<>();
    }

    /**
     * todo javadoc
     * */
    public void setRoleAssignments(Set<RoleAssignment> roleAssignments) {
        Set<String> roleIds = new HashSet<>();
        Set<String> roleAssignmentIds = new HashSet<>();

        roleAssignments.forEach(assignment -> {
            roleIds.add(assignment.getRoleId());
            roleAssignmentIds.add(assignment.getStringId());
        });

        this.roleAssignments = roleAssignmentIds;
        this.roles = roleIds;
    }

    public boolean isAdmin() {
        return getAuthorities().contains(new Authority(Authority.admin));
    }

    public String getEmail() {
        return getUsername();
    }

    /**
     * todo javadoc
     * */
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
        if (this.isImpersonating()) {
            user.setImpersonated(this.getImpersonated().transformToUser());
        }

        return user;
    }

    /**
     * todo javadoc
     * */
    public AnonymousUser transformToAnonymousUser() {
        AnonymousUser anonym = new AnonymousUser(new ObjectId(this.id));

        anonym.setEmail(getUsername());
        anonym.setName("Anonymous");
        anonym.setSurname("User");
        anonym.setPassword("n/a");
        anonym.setState(UserState.ACTIVE);
        anonym.setAuthorities(getAuthorities().stream().map(a -> ((Authority) a)).collect(Collectors.toSet()));
        anonym.setNextGroups(groups.stream().map(String::new).collect(Collectors.toSet()));

        return anonym;
    }

    /**
     * todo javadoc
     * */
    public Author transformToAuthor() {
        Author author = new Author();

        author.setId(this.id);
        author.setEmail(getUsername());
        author.setFullName(this.fullName);

        return author;
    }

    public void impersonate(LoggedUser toImpersonate) {
        this.impersonated = toImpersonate;
    }

    public void clearImpersonated() {
        this.impersonated = null;
    }

    public boolean isImpersonating() {
        return this.impersonated != null;
    }

    @JsonIgnore
    public LoggedUser getSelfOrImpersonated() {
        return this.isImpersonating() ? this.impersonated : this;
    }

    @Override
    public String toString() {
        return "LoggedUser{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", groups=" + groups +
                ", rolesAssignments=" + roleAssignments +
                ", roles=" + roles +
                ", impersonated=" + impersonated +
                '}';
    }
}