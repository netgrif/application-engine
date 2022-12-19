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
import java.util.Locale;
import java.util.stream.Collectors;

public class EnumerationFieldBuilder extends SelectionFieldBuilder {

    public EnumerationFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, EnumerationField field, int lastX, int lastY, Locale locale) {
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;

        if (field.getChoices() != null) {
            choices = field.getChoices().stream().map(choice -> choice.getTranslation(locale)).collect(Collectors.toList());
        }
        if (field.getValue() != null) {
            values.add(field.getValue().getValue().getTranslation(locale));
        }

        String translatedTitle = field.getName().getTranslation(locale);
        PdfSelectionField pdfField = new PdfEnumerationField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    public PdfField buildField(DataGroup dataGroup, EnumerationField field, int lastX, int lastY) {
        return buildField(dataGroup, field, lastX, lastY, LocaleContextHolder.getLocale());
    }
}
