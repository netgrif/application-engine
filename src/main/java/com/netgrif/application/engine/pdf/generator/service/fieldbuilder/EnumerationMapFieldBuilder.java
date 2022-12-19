package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfEnumerationField;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfSelectionField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EnumerationMapFieldBuilder extends SelectionFieldBuilder {

    public EnumerationMapFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, DataRef dataRef, int lastX, int lastY, Locale locale) {
        EnumerationMapField field = (EnumerationMapField) dataRef.getField();
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;
        if (field.getOptions() != null) {
            choices = field.getOptions().values().stream().map(option -> option.getTranslation(locale)).collect(Collectors.toList());
        }
        if (field.getValue() != null) {
            values.add(field.getOptions().get(field.getValue().getValue()).getTranslation(locale));
        }
        String translatedTitle = field.getName().getTranslation(locale);
        PdfSelectionField pdfField = new PdfEnumerationField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, dataRef, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    public PdfField buildField(DataGroup dataGroup, DataRef field, int lastX, int lastY) {
        return buildField(dataGroup, field, lastX, lastY, LocaleContextHolder.getLocale());
    }
}
