package com.netgrif.workflow.elastic.domain.mapping;

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
public class UserField extends DataField {

    @Field(type = Text)
    private String email;

    @Field(type = Text)
    private String fullName;

    public UserField(long value, String email, String fullName) {
        super("" + value);
        this.email = email;
        this.fullName = fullName;
    }
}


