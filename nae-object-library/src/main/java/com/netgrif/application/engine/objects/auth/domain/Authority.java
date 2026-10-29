package com.netgrif.application.engine.objects.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;

/**
 * Abstract base class representing an authority (permission/role) in the system.
 * Implements {@link Serializable} to support object serialization.
 */
@Getter
@NoArgsConstructor
public abstract class Authority implements Serializable {

    /**
     * Serial version UID for serialization support.
     */
    @Serial
    private static final long serialVersionUID = 2839744057647464485L;

    /**
     * Constant representing the administrator role.
     */
    public static final String admin = "ADMIN";

    /**
     * Constant representing the system administrator role.
     */
    public static final String systemAdmin = "SYSTEMADMIN";

    /**
     * Constant representing the standard user role.
     */
    public static final String user = "USER";

    /**
     * Constant representing the anonymous user role.
     */
    public static final String anonymous = "ANONYMOUS";

    /**
     * MongoDB ObjectId of the authority.
     */
    private ObjectId _id;

    /**
     * The name of the authority. This field cannot be null and is excluded from JSON serialization.
     */
    @NotNull
    @JsonIgnore
    @Setter
    private String name;

    /**
     * Constructs a new Authority with the specified name.
     * @param name the name of the authority
     */
    public Authority(String name) {
        this.name = name;
    }

    /**
     * Copy constructor for creating a new Authority from an existing one.
     * @param authority the authority to copy from
     */
    public Authority(Authority authority) {
        this._id = authority.get_id();
        this.name = authority.getName();
    }

    /**
     * Returns the string representation of the authority's ID.
     * @return the authority's ID as a string
     */
    public String getStringId() {
        return _id.toString();
    }

    /**
     * Returns the authority's name.
     * @return the authority name
     */
    public String getAuthority() {
        return this.name;
    }

    /**
     * Sets the authority's name.
     * @param authority the new authority name
     */
    public void setAuthority(String authority) {
        this.name = authority;
    }

    /**
     * Checks if this authority equals another object.
     * Two authorities are considered equal if they have the same name.
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
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

    /**
     * Returns a string representation of the Authority object.
     * @return a string containing the authority's ID and name
     */
    @Override
    public String toString() {
        return "Authority{" +
                "id=" + _id +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * Returns the hash code for this authority.
     * The hash code is based on the authority's name.
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}