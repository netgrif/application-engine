package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileField extends com.netgrif.application.engine.objects.elastic.domain.FileField {

    public FileField(FileField field) {
        super(field);
    }

    public FileField(FileFieldValue value) {
        super(value);
    }

    public FileField(FileFieldValue[] values) {
        super(values);
    }

    @Override
    @Field(type = Text)
    public String[] getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Text)
    public String[] getFileNameValue() {
        return super.getFileNameValue();
    }

    @Field(type = Keyword)
    public String[] getFileExtensionValue() {
        return super.getFileExtensionValue();
    }
}
