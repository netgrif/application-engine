package com.netgrif.application.engine.adapter.spring.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskField extends com.netgrif.application.engine.objects.elastic.domain.TaskField {

    public TaskField(String[] values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Override
    @Field(type = Keyword)
    public String[] getTaskRefValue() {
        return super.getTaskRefValue();
    }
}
