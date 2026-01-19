package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Integer;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ButtonField extends com.netgrif.application.engine.objects.elastic.domain.ButtonField {

    public ButtonField(ButtonField field) {
        super(field);
    }

    public ButtonField(Integer value) {
        super(value);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Integer)
    public Integer getButtonValue() {
        return buttonValue;
    }
}
