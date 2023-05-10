package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import lombok.Data;
import org.springframework.util.CollectionUtils;

@Data
public abstract class PdfFieldBuilder<T extends PdfField<?>> {

    protected PdfResource resource;

    protected int lastX, lastY;

    public PdfFieldBuilder() {
    }

    public abstract T buildField(PdfBuildingBlock buildingBlock);

    public abstract String[] getType();

    protected abstract void setupValue(PdfBuildingBlock buildingBlock,T pdfField);

    protected abstract int countValueMultiLineHeight(T pdfField);

    protected abstract void setFieldParams(PdfBuildingBlock buildingBlock, T pdfField);

    public static <T extends PdfField<?>> int countTopPosY(T field, PdfResource resource) {
        return resource.getBaseY() - ((field.getLayoutY() * resource.getFormGridRowHeight()) + resource.getPadding());
    }

    public static <T extends PdfField<?>> int countBottomPosY(T field, PdfResource resource) {
        return resource.getBaseY() - ((field.getLayoutY() * resource.getFormGridRowHeight()) + field.getHeight() + resource.getPadding());
    }

    public boolean changeHeight(int multiLineHeight, T pdfField) {
        if (multiLineHeight <= pdfField.getHeight()) {
            return false;
        }
        pdfField.setHeight(multiLineHeight);
        return true;
    }

    protected void setFieldPositions(T pdfField) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField, resource));
        pdfField.setTopY(countTopPosY(pdfField, resource));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField, resource));
        pdfField.setBottomY(countBottomPosY(pdfField, resource));
        pdfField.setChangedSize(changeHeight(countMultiLineHeight(pdfField), pdfField));
    }

    protected int countMultiLineHeight(T pdfField) {
        int multiLineHeight = 0;
        if (!CollectionUtils.isEmpty(pdfField.getLabel())) {
            multiLineHeight += countLabelMultiLineHeight(pdfField);
        }
        if (!pdfField.isValueEmpty()) {
            multiLineHeight += countValueMultiLineHeight(pdfField);
        }
        return multiLineHeight;
    }

    protected int countFieldHeight() {
        return resource.getFormGridRowHeight() - resource.getPadding();
    }

    private int countLabelMultiLineHeight(T pdfField) {
        return pdfField.getLabel().size() * resource.getLineHeight() + resource.getPadding();
    }

    private int countPosX(T field) {
        return resource.getBaseX() + (field.getLayoutX() * resource.getFormGridColWidth() + resource.getPadding());
    }

}
