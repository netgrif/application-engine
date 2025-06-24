package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserListFieldValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 5228212326431238485L;
    private Set<UserFieldValue> userValues;

    public UserListFieldValue() {
        this.userValues = new LinkedHashSet<UserFieldValue>();
    }

    public UserListFieldValue(Collection<UserFieldValue> userValues) {
        this();
        this.userValues = new LinkedHashSet<UserFieldValue>(userValues);
    }

    public LinkedHashSet<UserFieldValue> getUserValues() {
        return (LinkedHashSet<UserFieldValue>) userValues;
    }

    public void setUserValues(Collection<UserFieldValue> userValues) {
        this.userValues = new LinkedHashSet<UserFieldValue>(userValues);
    }

    @Override
    public String toString() {
        return userValues.toString();
    }
}
