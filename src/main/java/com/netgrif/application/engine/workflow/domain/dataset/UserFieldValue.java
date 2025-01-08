package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.auth.domain.IUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: release/8.0.0 remove, store only ObjectId
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFieldValue {

    private String id;
    private String name;
    private String surname;
    private String email;

    public UserFieldValue(IUser user) {
        this.id = user.getStringId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
    }

    public String getFullName() {
        return name + " " + surname;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserFieldValue)) {
            return false;
        }
        return this.id != null && ((UserFieldValue) obj).id != null && this.id.equals(((UserFieldValue) obj).id);
    }
}