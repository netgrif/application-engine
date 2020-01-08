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
    private String email;

    @Field(type = Text)
    private String fullName;

    @Field(type = FieldType.Long)
    private Long userId;

    public UserField(long userId, String email, String fullName) {
        super(String.format("%s %s", fullName, email));
        this.email = email;
        this.fullName = fullName;
        this.userId = userId;
    }
}


