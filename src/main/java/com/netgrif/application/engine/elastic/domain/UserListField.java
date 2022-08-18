package com.netgrif.application.engine.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserListField extends UserField {

    @Field(type = Text)
    private String[] emailValue;

    @Field(type = Text)
    private String[] fullNameValue;

    @Field(type = Text)
    private String[] userIdValue;

    public UserListField(UserMappingData[] values) {
        super(values);
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

}
