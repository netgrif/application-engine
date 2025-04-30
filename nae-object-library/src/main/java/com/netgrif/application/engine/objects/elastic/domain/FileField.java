package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FileField extends DataField {

    public String[] fileNameValue;

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
