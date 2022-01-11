package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.FileListField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedFileListField extends LocalisedField {

    public LocalisedFileListField(FileListField field, Locale locale) {
        super(field, locale);
    }
}
