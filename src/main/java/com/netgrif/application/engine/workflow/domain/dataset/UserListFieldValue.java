package com.netgrif.application.engine.workflow.domain.dataset;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class UserListFieldValue {

    private LinkedHashSet<UserFieldValue> userValues;

    public UserListFieldValue(Collection<UserFieldValue> userValues) {
        this.userValues = new LinkedHashSet<>(userValues);
    }

    public UserListFieldValue(UserFieldValue userValue) {
        this(Set.of(userValue));
    }

    @Override
    public String toString() {
        if (userValues == null) {
            return "";
        }
        return userValues.toString();
    }
}
