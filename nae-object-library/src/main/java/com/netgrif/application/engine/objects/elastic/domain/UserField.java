package com.netgrif.application.engine.objects.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    public UserField(UserMappingData value) {
        super(String.format("%s %s", value.fullName, value.email));
        this.usernameValue = new String[1];
        this.fullNameValue = new String[1];
        this.userIdValue = new String[1];
        this.userRealmIdValue = new String[1];
        this.usernameValue[0] = value.email;
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
            this.usernameValue[i] = values[i].email;
            this.fullNameValue[i] = values[i].fullName;
            this.userIdValue[i] = values[i].userId;
            this.userRealmIdValue[i] = values[i].userRealmId;
            super.fulltextValue[i] = String.format("%s %s", values[i].fullName, values[i].email);
        }
    }

    @AllArgsConstructor
    public static class UserMappingData {
        public String userId;
        public String userRealmId;
        public String email;
        public String fullName;
    }
}


