package com.netgrif.application.engine.authentication.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Identity extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 3031325636490953409L;

    @Setter
    protected String id;

    @Setter
    protected String fullName;

    @Setter
    protected boolean anonymous;

    private Identity impersonated;

    public Identity(String id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public boolean isAdmin() {
        return getAuthorities().contains(new Authority(Authority.admin));
    }

    public String getEmail() {
        return getUsername();
    }

    public void impersonate(Identity toImpersonate) {
        this.impersonated = toImpersonate;
    }

    public void clearImpersonated() {
        this.impersonated = null;
    }

    public boolean isImpersonating() {
        return this.impersonated != null;
    }

    @JsonIgnore
    public Identity getSelfOrImpersonated() {
        return this.isImpersonating() ? this.impersonated : this;
    }

    @Override
    public String toString() {
        return "LoggedUser{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", impersonated=" + impersonated +
                '}';
    }
}