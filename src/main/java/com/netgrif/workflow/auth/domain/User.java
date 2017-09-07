package com.netgrif.workflow.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.jasypt.hibernate4.type.EncryptedStringType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@TypeDef(
        name = "encryptedStr",
        defaultForType = EncryptedStringType.class,
        typeClass = EncryptedStringType.class,
        parameters = {
                @org.hibernate.annotations.Parameter(name = "algorithm", value = "PBEWITHSHA1ANDRC4_128"),
                @org.hibernate.annotations.Parameter(name = "password", value = "s56k3N5xh782")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @NotNull
    @Email
    @Column(unique = true)
    @Getter @Setter
    private String email;

    @Getter @Setter
    private String telNumber;

    @Getter @Setter
    private String avatar;

    @JsonIgnore
    @NotNull
    @Length(min = 6)
    @Getter @Setter
    private String password;

    @NotNull
    @NotBlank
    @Getter @Setter
    @Type(type = "encryptedStr")
    private String name;

    @NotNull
    @NotBlank
    @Getter @Setter
    private String surname;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_organizations", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "organization_id"))
    @Getter @Setter
    private Set<Organization> organizations;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
    @Getter @Setter
    private Set<Authority> authorities;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_process_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "user_process_role_id"))
    @Getter @Setter
    private Set<UserProcessRole> userProcessRoles;

    @Transient
    @Getter @Setter
    private Set<ProcessRole> processRoles;

    public User() {
        organizations = new HashSet<>();
        authorities = new HashSet<>();
        userProcessRoles = new HashSet<>();
    }

    public User(Long id) {
        this();
        this.id = id;
    }

    public User(String email, String password, String name, String surname) {
        this();
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public User(ObjectNode json){
        this(json.get("email").asText(),null,json.get("name").asText(),json.get("surname").asText());
        ((ArrayNode)json.get("userProcessRoles"))
                .forEach(node -> userProcessRoles.add(new UserProcessRole( node.get("roleId").asText())));
    }

    public void addAuthority(Authority authority) {
        authorities.add(authority);
    }

    public void addProcessRole(UserProcessRole role) {
        userProcessRoles.add(role);
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public void addOrganization(Organization org){
        this.organizations.add(org);
    }
}