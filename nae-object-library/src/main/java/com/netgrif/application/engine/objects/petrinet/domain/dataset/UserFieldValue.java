package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.elastic.domain.ActorMappingData;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserFieldValue extends ActorFieldValue {

    protected String firstName;
    protected String lastName;
    protected String username;

    public UserFieldValue(String id, String realmId, String firstName, String lastName, String username) {
        super(id, realmId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    public UserFieldValue(AbstractUser user) {
        this(user.getStringId(), user.getRealmId(), user.getFirstName(), user.getLastName(), user.getUsername());
    }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "UserFieldValue{" + ", name='" + firstName + "'" + ", surname='" + lastName + "'" + ", username='" + username + "'" + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserFieldValue)) {
            return false;
        }
        return this.id != null && ((UserFieldValue) obj).getId() != null && this.id.equals(((UserFieldValue) obj).getId());
    }

    @Override
    public ActorMappingData buildMappingData() {
        return new ActorMappingData(this.id, this.realmId, this.username, this.getFullName());
    }
}
