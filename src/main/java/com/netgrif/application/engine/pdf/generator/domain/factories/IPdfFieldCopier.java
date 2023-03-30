package com.netgrif.application.engine.pdf.generator.domain.factories;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;

public interface IPdfFieldCopier<U, T extends PdfField<U>> {

    default T copyOf(T field) {
        T copy = (T) field.newInstance();
        copy.setFieldId(field.getFieldId());
        copy.setComponent(field.getComponent());
        copy.setLabel(field.getLabel());
        copy.setValue(field.getValue());
        copy.setLayoutX(field.getLayoutX());
        copy.setLayoutY(field.getLayoutY());
        copy.setX(field.getX());
        copy.setOriginalTopY(field.getOriginalTopY());
        copy.setTopY(field.getTopY());
        copy.setOriginalBottomY(field.getOriginalBottomY());
        copy.setBottomY(field.getBottomY());
        copy.setWidth(field.getWidth());
        copy.setHeight(field.getHeight());
        copy.setChangedSize(field.isChangedSize());
        return copy;
    }
}
