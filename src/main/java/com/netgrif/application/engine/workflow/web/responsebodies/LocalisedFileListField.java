package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import lombok.Data;

import java.util.Locale;

@Data
public class LocalisedFileListField extends LocalisedField {

    public LocalisedFileListField(FileListField field, Locale locale) {
        super(field, locale);
    }
}
