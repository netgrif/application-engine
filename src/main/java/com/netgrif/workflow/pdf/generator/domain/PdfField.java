package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.service.fieldbuilder.FieldBuilder;
import com.netgrif.workflow.pdf.generator.service.renderer.Renderer;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class that holds information about fields that will be exported to PDF
 */
public abstract class PdfField implements Comparable<PdfField> {

    @Getter
    @Setter
    protected String fieldId;

    @Getter
    @Setter
    protected DataGroup dataGroup = null;

    @Getter
    @Setter
    protected FieldType type = null;

    @Getter
    @Setter
    protected String label;

    @Getter
    @Setter
    protected List<String> values = null;

    @Getter
    @Setter
    protected int layoutX;

    @Getter
    @Setter
    protected int layoutY;

    @Getter
    @Setter
    protected int x;

    @Getter
    @Setter
    protected int originalTopY;

    @Getter
    @Setter
    protected int topY;

    @Getter
    @Setter
    protected Integer originalBottomY;

    @Getter
    @Setter
    protected int bottomY;

    @Getter
    @Setter
    protected int width;

    @Getter
    @Setter
    protected int height;

    @Getter
    @Setter
    protected boolean changedSize = false;

    @Getter
    @Setter
    protected boolean changedPosition = false;

    @Getter
    @Setter
    protected boolean dgField = false;

    @Getter
    @Setter
    protected PdfResource resource;

    @Getter
    @Setter
    protected Renderer renderer;

    public PdfField(PdfResource resource){
        this.resource = resource;
    }

    public void countMultiLineHeight(int fontSize, PdfResource resource) {
        int padding = resource.getPadding();
        int lineHeight = resource.getLineHeight();
        int maxLabelLineLength = getMaxLabelLineSize(this.width, fontSize, padding);
        int maxValueLineLength = getMaxValueLineSize(this.width - 3 * padding, resource.getFontValueSize(), padding);
        int multiLineHeight = 0;

        List<String> splitLabel = FieldBuilder.generateMultiLineText(Collections.singletonList(this.label), maxLabelLineLength);
        multiLineHeight += splitLabel.size() * lineHeight + padding;

        if (this.values != null) {
            List<String> splitText = FieldBuilder.generateMultiLineText(this.values, maxValueLineLength);
            multiLineHeight += splitText.size() * lineHeight + padding;
        }
        this.changedSize = changeHeight(multiLineHeight);
    }

    protected boolean changeHeight(int multiLineHeight) {
        if (multiLineHeight <= this.height) {
            return false;
        }
        this.height = multiLineHeight;
        return true;
    }

    @Override
    public int compareTo(PdfField pdfField) {
        return this.getOriginalBottomY().compareTo(pdfField.getOriginalBottomY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdfField field = (PdfField) o;
        return fieldId == field.fieldId &&
                type == field.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, type);
    }

    protected int getMaxLabelLineSize(int fieldWidth, int fontSize, int padding) {
        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
    }

    protected int getMaxValueLineSize(int fieldWidth, int fontSize, int padding) {
        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
    }
}
