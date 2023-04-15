package com.netgrif.application.engine.pdf.generator.domain.fields;

import com.netgrif.application.engine.pdf.generator.domain.factories.IPdfFieldCopier;
import com.netgrif.application.engine.pdf.generator.domain.factories.PdfFieldCopier;
import lombok.Getter;
import lombok.Setter;

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
    protected String component;

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

//    @Getter
//    @Setter
//    protected boolean dgField;

//    @Getter
//    @Setter
//    protected PdfResource resource;

//    @Getter
//    @Setter
//    protected Renderer renderer;

    public PdfField() {
        //dgField = false;
    }

    public PdfField(String fieldId) {
        this();
        this.fieldId = fieldId;
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

    public boolean isLabelEmpty() {
        return label == null || label.isEmpty();
    }

    public abstract String getType();

    public abstract boolean isValueEmpty();

    public abstract PdfField<T> newInstance();

    public PdfFieldCopier<T, ?> getCopier() {
        return new PdfFieldCopier<>(this);
    }

//    public static <T> PdfField<T> copyOf(PdfField<T> field) {
//        PdfField<T> copy = field.newInstance();
//        copy.setFieldId(field.getFieldId());
//        copy.setLabel(field.getLabel());
//        copy.setLayoutX(field.getLayoutX());
//        copy.setLayoutY(field.getLayoutY());
//        copy.setX(field.getX());
//        copy.setOriginalTopY(field.getOriginalTopY());
//        copy.setTopY(field.getTopY());
//        copy.setOriginalBottomY(field.getOriginalBottomY());
//        copy.setBottomY(field.getBottomY());
//        copy.setWidth(field.getWidth());
//        copy.setHeight(field.getHeight());
//        copy.setChangedSize(field.isChangedSize());
//        return copy;
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
        return Objects.equals(fieldId, field.fieldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId);
    }


}
