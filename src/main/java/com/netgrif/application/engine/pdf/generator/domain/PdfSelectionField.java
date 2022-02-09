package com.netgrif.application.engine.pdf.generator.domain;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.FieldBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

public abstract class PdfSelectionField extends PdfField {

    public PdfSelectionField(PdfResource resource) {
        super(resource);
    }

    @Getter
    @Setter
    protected List<String> choices = null;

    @Override
    public void countMultiLineHeight(int fontSize, PdfResource resource) {
        int padding = resource.getPadding();
        int lineHeight = resource.getLineHeight();
        int maxLabelLineLength = getMaxLabelLineSize(this.width, fontSize, padding);
        int maxValueLineLength = getMaxValueLineSize(this.width - 2 * padding, resource.getFontValueSize(), padding);
        int multiLineHeight = 0;

        List<String> splitLabel = FieldBuilder.generateMultiLineText(Collections.singletonList(this.label), maxLabelLineLength);
        multiLineHeight += splitLabel.size() * lineHeight + padding;

        if (this.choices != null) {
            List<String> splitText = FieldBuilder.generateMultiLineText(this.choices, maxValueLineLength);
            multiLineHeight += splitText.size() * lineHeight + padding;
        }
        this.changedSize = changeHeight(multiLineHeight);
    }
}
