package com.netgrif.application.engine.authentication.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

/**
 * todo javadoc
 */
@Getter
public class Authority implements GrantedAuthority, Serializable {

    private static final long serialVersionUID = 2839744057647464485L;

    private final String applicationAssignmentId;
    private final String applicationRoleId;

    public Authority(String applicationAssignmentId, String applicationRoleId) {
        this.applicationAssignmentId = applicationAssignmentId;
        this.applicationRoleId = applicationRoleId;
    }

    /**
     * todo javadoc
     */
    @Override
    public String getAuthority() {
        return this.applicationRoleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authority authority = (Authority) o;

        return this.applicationRoleId.equals(authority.getApplicationRoleId())
                && this.applicationAssignmentId.equals(authority.getApplicationAssignmentId());
    }

    @Override
    public String toString() {
        return "Authority{" +
                "assignmentId=" + applicationAssignmentId +
                ", roleId='" + applicationRoleId + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return applicationRoleId.hashCode();
    }
}
