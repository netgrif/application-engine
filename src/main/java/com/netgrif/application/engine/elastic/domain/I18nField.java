package com.netgrif.application.engine.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.*;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class I18nField extends TextField {

    @Field(type = Text)
    public String[] keyValue;

    public I18nField(Set<String> keys, Set<String> values) {
        super(new String[0]);
        this.keyValue = keys.toArray(new String[0]);
        this.textValue = values.toArray(new String[0]);
    }
}
