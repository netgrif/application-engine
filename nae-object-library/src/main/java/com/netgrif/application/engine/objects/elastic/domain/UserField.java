package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.UserFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.IntStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class UserField extends DataField {

    // TODO JOFO: put group into userField
    private String[] usernameValue;

    private String[] fullNameValue;

    private String[] userIdValue;

    private String[] userRealmIdValue;

    public UserField(UserField field) {
        super(field);
        this.usernameValue = field.usernameValue == null ? null : Arrays.copyOf(field.usernameValue, field.usernameValue.length);
        this.fullNameValue = field.fullNameValue == null ? null : Arrays.copyOf(field.fullNameValue, field.fullNameValue.length);
        this.userIdValue = field.userIdValue == null ? null : Arrays.copyOf(field.userIdValue, field.userIdValue.length);
        this.userRealmIdValue = field.userRealmIdValue == null ? null : Arrays.copyOf(field.userRealmIdValue, field.userRealmIdValue.length);
    }

    public UserField(UserMappingData value) {
        super(String.format("%s %s", value.fullName, value.username));
        this.usernameValue = new String[1];
        this.fullNameValue = new String[1];
        this.userIdValue = new String[1];
        this.userRealmIdValue = new String[1];
        this.usernameValue[0] = value.username;
        this.fullNameValue[0] = value.fullName;
        this.userIdValue[0] = value.userId;
        this.userRealmIdValue[0] = value.userRealmId;
    }

    public UserField(UserMappingData[] values) {
        super(new String[values.length]);
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

    @Override
    public Object getValue() {
        if (userIdValue != null && userIdValue.length == 1) {
            String fullName = fullNameValue[0] != null ? fullNameValue[0] : "";
            String[] fullNameSplit = fullName.split(" ", 2);
            String firstName = fullNameSplit.length > 0 ? fullNameSplit[0] : "";
            String lastName = fullNameSplit.length > 1 ? fullNameSplit[1] : "";
            return new UserFieldValue(userIdValue[0], userRealmIdValue[0], firstName, lastName, usernameValue[0]);
        } else if (userIdValue != null && userIdValue.length > 1) {
            return IntStream.range(0, userIdValue.length).mapToObj(i -> {
                String fullName = fullNameValue[i] != null ? fullNameValue[i] : "";
                String[] fullNameSplit = fullName.split(" ", 2);
                String firstName = fullNameSplit.length > 0 ? fullNameSplit[0] : "";
                String lastName = fullNameSplit.length > 1 ? fullNameSplit[1] : "";
                return new UserFieldValue(userIdValue[i], userRealmIdValue[i], firstName, lastName, usernameValue[i]);
            }).toList();
        }
        return null;
    }

    @AllArgsConstructor
    public static class UserMappingData {
        public String userId;
        public String userRealmId;
        public String username;
        public String fullName;
    }
}


