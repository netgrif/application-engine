package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfI18nDividerField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class I18nDividerFieldBuilder extends FieldBuilder {

    public I18nDividerFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, DataRef dataRef, int lastX, int lastY, Locale locale) {
        I18nField field = (I18nField) dataRef.getField();
        this.lastX = lastX;
        this.lastY = lastY;
        String value = field.getValue().getValue().getTranslation(locale);
        String translatedTitle = field.getName().getTranslation(locale);
        PdfField pdfField = new PdfI18nDividerField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, dataRef, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    public PdfField buildField(DataGroup dataGroup, DataRef field, int lastX, int lastY) {
        return buildField(dataGroup, field, lastX, lastY, LocaleContextHolder.getLocale());
    }
}
