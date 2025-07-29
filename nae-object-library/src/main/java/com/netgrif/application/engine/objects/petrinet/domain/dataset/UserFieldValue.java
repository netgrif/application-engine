package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Getter
@NoArgsConstructor
public class UserFieldValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 5228212326436828485L;
    protected String id;
    protected String realmId;
    protected String firstName;
    protected String lastName;
    protected String username;

    public UserFieldValue(String id, String realmId, String firstName, String lastName, String username) {
        this.id = id;
        this.realmId = realmId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    public UserFieldValue(AbstractUser user) {
        this(user.getStringId(), user.getRealmId(), user.getFirstName(), user.getLastName(), user.getUsername());
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "UserFieldValue{" + ", name='" + firstName + "'" + ", surname='" + lastName + "'" + ", username='" + username + "'" + "}";
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserFieldValue)) {
            return false;
        }
        return this.id != null && ((UserFieldValue) obj).getId() != null && this.id.equals(((UserFieldValue) obj).getId());
    }
}
