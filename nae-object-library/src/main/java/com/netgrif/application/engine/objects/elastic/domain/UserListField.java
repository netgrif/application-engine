package com.netgrif.application.engine.objects.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class UserListField extends UserField {

    private String[] usernameValue;

    private String[] fullNameValue;

    private String[] userIdValue;

    private String[] userRealmIdValue;

    public UserListField(UserListField field) {
        super(field);
        this.usernameValue = field.usernameValue == null ? null : Arrays.copyOf(field.usernameValue, field.usernameValue.length);
        this.fullNameValue = field.fullNameValue == null ? null : Arrays.copyOf(field.fullNameValue, field.fullNameValue.length);
        this.userIdValue = field.userIdValue == null ? null : Arrays.copyOf(field.userIdValue, field.userIdValue.length);
        this.userRealmIdValue = field.userRealmIdValue == null ? null : Arrays.copyOf(field.userRealmIdValue, field.userRealmIdValue.length);
    }

    public UserListField(UserMappingData[] values) {
        super(values);
        this.usernameValue = new String[values.length];
        this.fullNameValue = new String[values.length];
        this.userIdValue = new String[values.length];
        this.userRealmIdValue = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            this.usernameValue[i] = values[i].username;
            this.fullNameValue[i] = values[i].fullName;
            this.userIdValue[i] = values[i].userId;
            this.userRealmIdValue[i] = values[i].userRealmId;
            super.fulltextValue[i] = String.format("%s %s", values[i].fullName, values[i].username);
        }
    }

}
