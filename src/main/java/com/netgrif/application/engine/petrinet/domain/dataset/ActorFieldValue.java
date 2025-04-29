package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.authorization.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: release/8.0.0 remove, store only ObjectId
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActorFieldValue {

    private String id;
    private String firstname;
    private String lastname;
    private String email;

    public ActorFieldValue(User user) {
        this.id = user.getStringId();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
    }

    public String getFullName() {
        return String.join(" ", firstname, lastname);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ActorFieldValue)) {
            return false;
        }
        return this.id != null && ((ActorFieldValue) obj).id != null && this.id.equals(((ActorFieldValue) obj).id);
    }
}