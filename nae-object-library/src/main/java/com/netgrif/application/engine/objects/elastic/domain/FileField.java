package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FileField extends DataField {

    public String[] filePath;

    public String[] fileNameValue;

    public String[] fileExtensionValue;

    public FileField(FileField fileField) {
        super(fileField);
        this.filePath = fileField.getFilePath() == null ? null : Arrays.copyOf(fileField.filePath, fileField.filePath.length);
        this.fileNameValue = fileField.getFileNameValue() == null ? null : Arrays.copyOf(fileField.fileNameValue, fileField.fileNameValue.length);
        this.fileExtensionValue = fileField.getFileExtensionValue() == null ? null : Arrays.copyOf(fileField.fileExtensionValue, fileField.fileExtensionValue.length);
    }

    public FileField(FileFieldValue value) {
        super(value.getName());
        this.filePath = new String[1];
        this.fileNameValue = new String[1];
        this.fileExtensionValue = new String[1];
        FileNameAndExtension extracted = this.extractFileExtensionFromName(value.getName());
        this.filePath[0] = value.getPath();
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

    @Override
    public Object getValue() {
        if (fileNameValue != null && fileNameValue.length == 1) {
            return new FileFieldValue(fileNameValue[0] + "." + fileExtensionValue[0], filePath[0]);
        } else if (fileNameValue != null && fileNameValue.length > 1) {
            return IntStream.range(0, fileNameValue.length).mapToObj(i -> new FileFieldValue(fileNameValue[i] + "." + fileExtensionValue[i], filePath[i])).toList();
        }
        return null;
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
