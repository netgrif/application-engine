package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfEnumerationField;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfSelectionField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationField;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnumerationFieldBuilder extends SelectionFieldBuilder {

    public EnumerationFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, EnumerationField field, int lastX, int lastY) {
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;

        if (field.getChoices() != null)
            choices = field.getChoices().stream().map(s -> s.getTranslation(LocaleContextHolder.getLocale())).collect(Collectors.toList());
        if (field.getValue() != null) {
//            TODO: NAE-1645
//            values.add(field.getValue().getTranslation(LocaleContextHolder.getLocale()));
        }

        String translatedTitle = field.getName().getTranslation(LocaleContextHolder.getLocale());
        PdfSelectionField pdfField = new PdfEnumerationField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
