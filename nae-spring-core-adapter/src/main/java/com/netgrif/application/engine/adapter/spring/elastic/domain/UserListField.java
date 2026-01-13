package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Arrays;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class UserListField extends com.netgrif.application.engine.objects.elastic.domain.UserListField {

    public UserListField(UserListField field) {
        super(field);
    }

    public UserListField(UserMappingData[] values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Text)
    public String[] getUsernameValue() {
        return super.getUsernameValue();
    }

    @Override
    @Field(type = Text)
    public String[] getFullNameValue() {
        return super.getFullNameValue();
    }

    @Override
    @Field(type = Text)
    public String[] getUserIdValue() {
        return super.getUserIdValue();
    }

    @Override
    @Field(type = Text)
    public String[] getUserRealmIdValue() {
        return super.getUserRealmIdValue();
    }
}
