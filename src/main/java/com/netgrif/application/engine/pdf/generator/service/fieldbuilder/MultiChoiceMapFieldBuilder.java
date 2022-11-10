package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfMultiChoiceField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiChoiceMapFieldBuilder extends SelectionFieldBuilder {

    public MultiChoiceMapFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, MultichoiceMapField field, int lastX, int lastY) {
        List<String> choices = new ArrayList<>();
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;

        if (field.getOptions() != null)
            choices = field.getOptions().values().stream().map(s -> s.getTranslation(LocaleContextHolder.getLocale())).collect(Collectors.toList());
        if (field.getValue() != null)
            values.addAll((field.getValue()).stream().map(value ->
                    field.getOptions().get(value).getTranslation(LocaleContextHolder.getLocale())).collect(Collectors.toList()));

        String translatedTitle = field.getName().getTranslation(LocaleContextHolder.getLocale());
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
