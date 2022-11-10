package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfI18nDividerField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import org.springframework.context.i18n.LocaleContextHolder;

public class I18nDividerFieldBuilder extends FieldBuilder {

    public I18nDividerFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, I18nField field, int lastX, int lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
        String value = field.getValue().getTranslation(LocaleContextHolder.getLocale());
        String translatedTitle = field.getName().getTranslation(LocaleContextHolder.getLocale());
        PdfField pdfField = new PdfI18nDividerField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
