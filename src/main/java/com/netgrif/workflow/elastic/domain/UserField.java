package com.netgrif.workflow.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserField extends DataField {

    @Field(type = Text)
    private String emailValue;

    @Field(type = Text)
    private String fullNameValue;

    @Field(type = FieldType.Long)
    private Long userIdValue;

    public UserField(long userId, String email, String fullName) {
        super(String.format("%s %s", fullName, email));
        this.emailValue = email;
        this.fullNameValue = fullName;
        this.userIdValue = userId;
    }
}


