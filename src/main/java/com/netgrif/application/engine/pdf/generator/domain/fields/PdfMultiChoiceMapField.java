package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfSelectionFieldCopier;

import java.util.Set;

public class PdfMultiChoiceMapField extends PdfSelectionField<Set<String>> {

    public PdfMultiChoiceMapField() {
        super();
    }

    public PdfMultiChoiceMapField(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return DataType.MULTICHOICE_MAP.value();
    }

    @Override
    public boolean isValueEmpty() {
        return value == null || value.isEmpty();
    }

    @Override
    public PdfMultiChoiceMapField newInstance() {
        return new PdfMultiChoiceMapField();
    }

    @Override
    public PdfSelectionFieldCopier<Set<String>> getCopier() {
        return new PdfSelectionFieldCopier<>(this);
    }
}
