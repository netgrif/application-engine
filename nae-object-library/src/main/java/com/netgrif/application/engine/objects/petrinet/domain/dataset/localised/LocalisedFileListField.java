package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileListField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedFileListField extends LocalisedField {

    public LocalisedFileListField(FileListField field, Locale locale) {
        super(field, locale);
    }
}
