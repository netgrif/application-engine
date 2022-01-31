package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfEnumerationField;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedEnumerationField;

import java.util.ArrayList;
import java.util.List;

public class EnumerationFieldBuilder extends SelectionFieldBuilder {

    public EnumerationFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedEnumerationField field, int lastX, int lastY){
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;

        if (field.getChoices() != null)
            choices = field.getChoices();
        if (field.getValue() != null) {
            values.add((String) field.getValue());
        }

        String translatedTitle = field.getName();
        PdfSelectionField pdfField = new PdfEnumerationField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
