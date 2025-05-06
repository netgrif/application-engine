package com.netgrif.application.engine.objects.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;

@Getter
@NoArgsConstructor
public abstract class Authority implements Serializable {

    @Serial
    private static final long serialVersionUID = 2839744057647464485L;

    public static final String admin = "ADMIN";
    public static final String systemAdmin = "SYSTEMADMIN";
    public static final String user = "USER";
    public static final String anonymous = "ANONYMOUS_USER";

    private ObjectId _id;

    @NotNull
    @JsonIgnore
    @Setter
    private String name;

    public Authority(String name) {
        this.name = name;
    }

    public Authority(Authority authority) {
        this._id = authority.get_id();
        this.name = authority.getName();
    }

    public String getStringId() {
        return _id.toString();
    }

    public String getAuthority() {
        return this.name;
    }

    public void setAuthority(String authority) {
        this.name = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
