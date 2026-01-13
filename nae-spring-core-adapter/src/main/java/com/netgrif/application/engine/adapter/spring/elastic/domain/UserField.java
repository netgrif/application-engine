package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
public class UserField extends com.netgrif.application.engine.objects.elastic.domain.UserField {

    public UserField(UserField field) {
        super(field);
    }

    public UserField(UserMappingData value) {
        super(value);
    }

    public UserField(UserMappingData[] values) {
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


