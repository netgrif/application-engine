package com.netgrif.workflow.elastic.domain;

import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileField extends DataField {

    @Field(type = FieldType.Text)
    public String[] fileNameValue;

    @Field(type = FieldType.Keyword)
    public String[] fileExtensionValue;

    public FileField(FileFieldValue value) {
        super(value.getName());
        this.fileNameValue = new String[1];
        this.fileExtensionValue = new String[1];
        FileNameAndExtension extracted = this.extractFileExtensionFromName(value.getName());
        this.fileNameValue[0] = extracted.name;
        this.fileExtensionValue[0] = extracted.extension;
    }

    public FileField(FileFieldValue[] values) {
        super(new String[values.length]);
        this.fileNameValue = new String[values.length];
        this.fileExtensionValue = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            FileNameAndExtension extracted = this.extractFileExtensionFromName(values[i].getName());
            this.fileNameValue[i] = extracted.name;
            this.fileExtensionValue[i] = extracted.extension;
            super.fulltextValue[i] = values[i].getName();
        }
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
