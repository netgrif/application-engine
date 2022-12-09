package com.netgrif.application.engine.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Text;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserField extends DataField {

    @Field(type = Text)
    private List<String> emailValue = new ArrayList<>();

    @Field(type = Text)
    private List<String> fullNameValue = new ArrayList<>();

    @Field(type = Text)
    private List<String> userIdValue = new ArrayList<>();

    public UserField(UserMappingData value) {
        super();
        this.addValue(value);
    }

    public UserField(List<UserMappingData> values) {
        super();
        values.forEach(this::addValue);
    }

    protected void addValue(UserMappingData value) {
        this.emailValue.add(value.email);
        this.fullNameValue.add(value.fullName);
        this.userIdValue.add(value.userId);
        super.fulltextValue.add(String.format("%s %s", value.fullName, value.email));
    }

    @AllArgsConstructor
    public static class UserMappingData {
        public String userId;
        public String email;
        public String fullName;
    }
}


