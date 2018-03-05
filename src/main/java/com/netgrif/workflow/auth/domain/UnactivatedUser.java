package com.netgrif.workflow.auth.domain;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class UnactivatedUser {

    public static final String VALUE_SEPARATOR = ";";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private Long id;

    @NotNull
    @NotBlank
    @Email
    @Column(unique = true)
    @Getter
    @Setter
    private String email;

    @NotNull
    @NotBlank
    @Getter
    @Setter
    private String token;

    @NotNull
    @Getter
    @Setter
    private LocalDateTime expirationDate;

    private String groups;

    private String authorities;

    private String processRoles;

    public UnactivatedUser() {
        this.expirationDate = LocalDateTime.now().plusDays(3);
    }

    public UnactivatedUser(String email, String token) {
        this();
        this.email = email;
        this.token = token;
    }

    public void setGroups(Set<Long> groups) {
        this.groups = join(groups);
    }

    public void setAuthorities(Set<Long> authorities) {
        this.authorities = join(authorities);
    }

    public void setProcessRoles(Set<String> processRoles) {
        this.processRoles = join(processRoles);
    }

    public Set<Long> getGroups() {
        return separate(this.groups, Long::parseLong);
    }

    public Set<Long> getAuthorities() {
        return separate(this.authorities, Long::parseLong);
    }

    public Set<String> getProcessRoles() {
        return separate(this.processRoles, String::new);
    }

    public static <T> String join(Set<T> objs) {
        StringBuilder builder = new StringBuilder();
        objs.forEach(o -> builder.append(o).append(VALUE_SEPARATOR));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static <T> Set<T> separate(String str, Function<String, T> parser) {
        Set<T> s = new LinkedHashSet<>();
        Set<String> items = Arrays.stream(str.split(VALUE_SEPARATOR)).collect(Collectors.toSet());
        items.forEach(item -> s.add(parser.apply(item)));
        return s;
    }
}
