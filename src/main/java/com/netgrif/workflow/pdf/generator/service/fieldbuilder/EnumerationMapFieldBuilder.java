package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfEnumerationField;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedEnumerationMapField;

import java.util.ArrayList;
import java.util.List;

public class EnumerationMapFieldBuilder extends SelectionFieldBuilder {

    public EnumerationMapFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedEnumerationMapField field, int lastX, int lastY){
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;

        if (field.getOptions() != null)
            choices = new ArrayList<>(field.getOptions().values());
        if (field.getValue() != null)
            values.add(field.getOptions().get(field.getValue()));

        String translatedTitle = field.getName();
        PdfSelectionField pdfField = new PdfEnumerationField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
