package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.service.fieldbuilder.FieldBuilder;
import com.netgrif.workflow.pdf.generator.service.renderer.Renderer;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Class that holds information about fields that will be exported to PDF
 */
@Data
public abstract class PdfField implements Comparable<PdfField> {

    protected String fieldId;

    protected DataGroup dataGroup = null;

    protected FieldType type = null;

    protected String label;

    protected List<String> values = null;

    protected int layoutX;

    protected int layoutY;

    protected int x;

    protected int originalTopY;

    protected int topY;

    protected Integer originalBottomY;

    protected int bottomY;

    protected int width;

    protected int height;

    protected boolean changedSize = false;

    protected boolean changedPosition = false;

    protected boolean dgField = false;

    protected PdfResource resource;

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

    protected int getMaxLabelLineSize(int fieldWidth, int fontSize, int padding) {
        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
    }

    protected int getMaxValueLineSize(int fieldWidth, int fontSize, int padding) {
        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
    }
}
