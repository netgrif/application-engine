package com.netgrif.application.engine.pdf.generator.domain;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.PdfFieldBuilder;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.layout.QDataGroupLayout;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class that holds information about fields that will be exported to PDF
 */
public abstract class PdfField<T> implements Comparable<PdfField<T>> {

    @Getter
    @Setter
    protected String fieldId;

    @Getter
    @Setter
    protected DataGroup dataGroup;

    @Getter
    @Setter
    protected DataType type;

    @Getter
    @Setter
    protected List<String> label;

    @Getter
    @Setter
    protected T value;

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
    protected boolean changedSize;

    @Getter
    @Setter
    protected boolean changedPosition;

    @Getter
    @Setter
    protected boolean dgField;

    @Getter
    @Setter
    protected PdfResource resource;

//    @Getter
//    @Setter
//    protected Renderer renderer;

    public PdfField() {
        changedSize = false;
        changedPosition = false;
        dgField = false;
    }

    public PdfField(PdfResource resource) {
        this();
        this.resource = resource;
    }

    public PdfField(String fieldId, DataGroup dataGroup, DataType type) {
        this();
        this.fieldId = fieldId;
        this.dataGroup = dataGroup;
        this.type = type;
    }

//    public void countMultiLineHeight(int fontSize, PdfResource resource) {
//        int padding = resource.getPadding();
//        int lineHeight = resource.getLineHeight();
//        int maxLabelLineLength = getMaxLabelLineSize(this.width, fontSize, padding);
//        int maxValueLineLength = getMaxValueLineSize(this.width - 3 * padding, resource.getFontValueSize(), padding);
//        int multiLineHeight = 0;
//
//        List<String> splitLabel = PdfFieldBuilder.generateMultiLineText(Collections.singletonList(this.label), maxLabelLineLength);
//        multiLineHeight += splitLabel.size() * lineHeight + padding;
//
//        if (this.value != null) {
//            List<String> splitText = PdfFieldBuilder.generateMultiLineText(this.value, maxValueLineLength);
//            multiLineHeight += splitText.size() * lineHeight + padding;
//        }
//        this.changedSize = changeHeight(multiLineHeight);
//    }

//    protected boolean changeHeight(int multiLineHeight) {
//        if (multiLineHeight <= this.height) {
//            return false;
//        }
//        this.height = multiLineHeight;
//        return true;
//    }

    @Override
    public int compareTo(PdfField pdfField) {
        return this.getOriginalBottomY().compareTo(pdfField.getOriginalBottomY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdfField<?> field = (PdfField<?>) o;
        return Objects.equals(fieldId, field.fieldId) &&
                type == field.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, type);
    }


}
