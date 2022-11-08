package com.netgrif.application.engine.elastic.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;
import static org.springframework.data.elasticsearch.annotations.FieldType.Text;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileField extends DataField {

    @Field(type = Text)
    public List<String> fileNameValue = new ArrayList<>();

    @Field(type = Keyword)
    public List<String> fileExtensionValue = new ArrayList<>();

    public FileField(FileFieldValue value) {
        super();
        this.addValue(value);
    }

    public FileField(Collection<FileFieldValue> values) {
        super();
        values.forEach(this::addValue);
    }

    private void addValue(FileFieldValue value) {
        FileNameAndExtension extracted = this.extractFileExtensionFromName(value.getName());
        this.fileNameValue.add(extracted.name);
        this.fileExtensionValue.add(extracted.extension);
        super.fulltextValue.add(extracted.name);
    }

    private FileNameAndExtension extractFileExtensionFromName(String filename) {
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            return new FileNameAndExtension(filename.substring(0, index), filename.substring(index + 1));
        }
        return new FileNameAndExtension(filename, null);
    }

    @AllArgsConstructor
    private static class FileNameAndExtension {
        public String name;
        public String extension;
    }
}
