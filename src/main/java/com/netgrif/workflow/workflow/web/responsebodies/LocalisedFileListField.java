package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.FileListField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.LocalizedValidation;
import com.netgrif.workflow.petrinet.domain.dataset.logic.validation.Validation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
public class LocalisedFileListField extends LocalisedField {

    // ValidableField
    private List<LocalizedValidation> validations;

    private Object defaultValue;

    public LocalisedFileListField(FileListField field, Locale locale) {
        super(field, locale);
        List<LocalizedValidation> locVal = new ArrayList<>();
        if (field.getValidations() != null) {
            for(Validation val:field.getValidations()){
                locVal.add(val.getLocalizedValidation(locale));
            }
        }
        this.validations = locVal;
        this.defaultValue = field.getDefaultValue();
    }
}
