package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

    @Deprecated
    public FileField(FileFieldValue[] values) {
        this(Arrays.asList(values));
    }

    public FileField(List<FileFieldValue> values) {
        super(values);
    }

    public FileField(HashSet<FileFieldValue> values) {
        this(new ArrayList<>(values));
    }

    @Override
    @Field(type = Text)
    public List<String> getFulltextValue() {
        return super.getFulltextValue();
    }

    @Field(type = Text)
    public List<String> getFileNameValue() {
        return super.getFileNameValue();
    }

    @Field(type = Keyword)
    public List<String> getFileExtensionValue() {
        return super.getFileExtensionValue();
    }
}
