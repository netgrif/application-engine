package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FileField extends DataField {

    protected List<String> filePath;
    protected List<String> fileNameValue;
    protected List<String> fileExtensionValue;

    public FileField(FileField fileField) {
        super(fileField);
        this.filePath = fileField.getFilePath() == null ? null : new ArrayList<>(fileField.filePath);
        this.fileNameValue = fileField.getFileNameValue() == null ? null : new ArrayList<>(fileField.fileNameValue);
        this.fileExtensionValue = fileField.getFileExtensionValue() == null ? null : new ArrayList<>(fileField.fileExtensionValue);
    }

    public FileField(FileFieldValue fileFieldValue) {
        this(fileFieldValue == null ? null : List.of(fileFieldValue));
    }

    public FileField(List<FileFieldValue> fileFieldValues) {
        if (fileFieldValues == null || fileFieldValues.isEmpty()) {
            return;
        }
        this.fileNameValue = new ArrayList<>();
        this.fileExtensionValue = new ArrayList<>();
        this.filePath = new ArrayList<>();
        for (FileFieldValue fileFieldValue : fileFieldValues) {
            if (fileFieldValue == null) {
                continue;
            }
            FileNameAndExtension extracted = this.extractFileExtensionFromName(fileFieldValue.getName());
            this.fileNameValue.add(extracted.name);
            this.fileExtensionValue.add(extracted.extension);
            this.fulltextValue.add(fileFieldValue.getName());
            this.filePath.add(fileFieldValue.getPath());
        }
    }

    @Override
    public Object getValue() {
        if (this.fileNameValue != null && this.fileNameValue.size() == 1) {
            return new FileFieldValue(nameWithExtension(this.fileNameValue.getFirst(), this.fileExtensionValue.getFirst()),
                    this.filePath.getFirst());
        } else if (this.fileNameValue != null && this.fileNameValue.size() > 1) {
            return IntStream.range(0, this.fileNameValue.size())
                    .mapToObj(i -> new FileFieldValue(nameWithExtension(this.fileNameValue.get(i), this.fileExtensionValue.get(i)),
                            this.filePath.get(i))).toList();
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

    private static String nameWithExtension(String fileName, String extension) {
        if (extension == null || extension.isEmpty()) {
            return fileName;
        }
        return fileName + "." + extension;
    }

    @AllArgsConstructor
    private static class FileNameAndExtension {
        public String name;
        public String extension;
    }
}
