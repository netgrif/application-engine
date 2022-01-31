package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfMultiChoiceField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedMultichoiceMapField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultiChoiceMapFieldBuilder extends SelectionFieldBuilder {

    public MultiChoiceMapFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedMultichoiceMapField field, int lastX, int lastY) {
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;

        if (field.getOptions() != null)
            choices = new ArrayList<>(field.getOptions().values());
        if (field.getValue() != null)
            values.addAll(((Collection<? extends String>) field.getValue()).stream().map(value ->
                    field.getOptions().get(value)).collect(Collectors.toList()));

        String translatedTitle = field.getName();
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
