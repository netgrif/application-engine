package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.UserFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.stream.IntStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class UserField extends DataField {

    private String[] emailValue;

    private String[] fullNameValue;

    private String[] userIdValue;

    public UserField(UserMappingData value) {
        super(String.format("%s %s", value.fullName, value.email));
        this.emailValue = new String[1];
        this.fullNameValue = new String[1];
        this.userIdValue = new String[1];
        this.emailValue[0] = value.email;
        this.fullNameValue[0] = value.fullName;
        this.userIdValue[0] = value.userId;
    }

    public UserField(UserMappingData[] values) {
        super(new String[values.length]);
        this.emailValue = new String[values.length];
        this.fullNameValue = new String[values.length];
        this.userIdValue = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            this.emailValue[i] = values[i].email;
            this.fullNameValue[i] = values[i].fullName;
            this.userIdValue[i] = values[i].userId;
            super.fulltextValue[i] = String.format("%s %s", values[i].fullName, values[i].email);
        }
    }

    @Override
    public Object getValue() {
        if (userIdValue != null && userIdValue.length == 1) {
            String fullName = fullNameValue[0] != null ? fullNameValue[0] : "";
            String[] fullNameSplit = fullName.split(" ", 2);
            String firstName = fullNameSplit.length > 0 ? fullNameSplit[0] : "";
            String lastName = fullNameSplit.length > 1 ? fullNameSplit[1] : "";
            return new UserFieldValue(userIdValue[0], firstName, lastName, emailValue[0]);
        } else if (userIdValue != null && userIdValue.length > 1) {
            return IntStream.range(0, userIdValue.length).mapToObj(i -> {
                String fullName = fullNameValue[i] != null ? fullNameValue[i] : "";
                String[] fullNameSplit = fullName.split(" ", 2);
                String firstName = fullNameSplit.length > 0 ? fullNameSplit[0] : "";
                String lastName = fullNameSplit.length > 1 ? fullNameSplit[1] : "";
                return new UserFieldValue(userIdValue[i], firstName, lastName, emailValue[i]);
            }).toList();
        }
        return null;
    }

    @AllArgsConstructor
    public static class UserMappingData {
        public String userId;
        public String email;
        public String fullName;
    }
}


