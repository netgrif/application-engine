package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.LayoutType;
import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import lombok.Data;
import lombok.Getter;

import java.util.*;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Data
public abstract class PdfFieldBuilder<T> {

    protected PdfResource resource;

    @Getter
    protected int lastX, lastY;

    public PdfFieldBuilder() {

    }

    public abstract PdfField<T> buildField(PdfFieldBuildingBlock buildingBlock);

    public abstract DataType getType();

    protected abstract void setupValue(PdfFieldBuildingBlock buildingBlock, PdfField<T> pdfField);

    protected abstract int countValueMultiLineHeight(PdfField<T> pdfField);

    public static int countTopPosY(PdfField<?> field, PdfResource resource) {
        return (field.getLayoutY() * resource.getFormGridRowHeight()) + resource.getPadding();
    }

    public static int countBottomPosY(PdfField<?> field, PdfResource resource) {
        return (field.getLayoutY() * resource.getFormGridRowHeight()) + field.getHeight() + resource.getPadding();
    }

    public boolean changeHeight(int multiLineHeight, PdfField<T> pdfField) {
        if (multiLineHeight <= pdfField.getHeight()) {
            return false;
        }
        pdfField.setHeight(multiLineHeight);
        return true;
    }



    protected void setFieldParams(PdfFieldBuildingBlock buildingBlock, PdfField<T> pdfField) {
        pdfField.setLayoutX(countFieldLayoutX(buildingBlock.getDataGroup(), buildingBlock.getDataRef()));
        pdfField.setLayoutY(countFieldLayoutY(buildingBlock.getDataGroup(), buildingBlock.getDataRef()));
        pdfField.setWidth(countFieldWidth(buildingBlock.getDataGroup(), buildingBlock.getDataRef()));
        setupLabel(buildingBlock, pdfField);
        setupValue(buildingBlock, pdfField);
        pdfField.setChangedSize(changeHeight(countMultiLineHeight(pdfField), pdfField));
    }

    protected void setFieldPositions(PdfField<T> pdfField) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField, resource));
        pdfField.setTopY(countTopPosY(pdfField, resource));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField, resource));
        pdfField.setBottomY(countBottomPosY(pdfField, resource));
        //countMultiLineHeight(fontSize, pdfField);
    }

//    protected int getMaxValueLineSize(int fieldWidth, int fontSize, int padding) {
//        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
//    }


//    protected int countFieldHeight() {
//        return resource.getFormGridRowHeight() - resource.getPadding();
//    }

    private int countFieldWidth(DataGroup dataGroup, DataRef field) {
        if (isDgFlow(dataGroup)) {
            return resource.getFormGridColWidth() - resource.getPadding();
        } else if (isDgLegacy(dataGroup)) {
            return (isStretch(dataGroup) ?
                    (resource.getFormGridColWidth() * resource.getFormGridCols())
                    : (resource.getFormGridColWidth() * resource.getFormGridCols() / 2)) - resource.getPadding();
        }
        return field.getLayout().getCols() * resource.getFormGridColWidth() - resource.getPadding();
    }

//    private int getMaxLabelLineSize(int fieldWidth, int fontSize, int padding) {
//        return (int) ((fieldWidth - padding) * resource.getSizeMultiplier() / fontSize);
//    }

    private boolean checkFullRow(DataGroup dataGroup, DataRef field) {
        return (isStretch(dataGroup)) ||
                (checkCol(field.getLayout()) && resource.getRowGridFree() < field.getLayout().getCols());
    }

    private boolean checkCol(FieldLayout layout) {
        return layout != null && layout.getCols() != null;
    }

    private boolean isStretch(DataGroup dataGroup) {
        return dataGroup.getStretch() != null && dataGroup.getStretch();
    }

    private boolean isDgFlow(DataGroup dataGroup) {
        return dataGroup.getLayout() != null && dataGroup.getLayout().getType() != null && dataGroup.getLayout().getType().equals(LayoutType.FLOW);
    }

    private boolean isDgLegacy(DataGroup dataGroup) {
        return dataGroup.getLayout() == null || dataGroup.getLayout().getType() == null || dataGroup.getLayout().getType().equals(LayoutType.LEGACY);
    }

    private void resolveRowGridFree(DataGroup dataGroup, FieldLayout layout) {
        if (checkCol(layout)) {
            resource.setRowGridFree(resource.getFormGridCols() - layout.getCols());
        } else {
            if (isStretch(dataGroup))
                resource.setRowGridFree(0);
            else {
                resource.setRowGridFree(resource.getFormGridCols() - 2);
            }
        }
    }

    private void setupLabel(PdfFieldBuildingBlock buildingBlock, PdfField<T> pdfField) {
        String translatedTitle = buildingBlock.getDataRef().getField().getName().getTranslation(buildingBlock.getLocale());
        int maxLabelLineLength = getMaxLineSize(pdfField.getWidth(), resource.getFontLabelSize(), resource.getPadding(), resource.getSizeMultiplier());
        List<String> label = generateMultiLineText(Collections.singletonList(translatedTitle), maxLabelLineLength);
        pdfField.setLabel(label);
    }

    private int countMultiLineHeight(PdfField<T> pdfField) {
        int multiLineHeight = 0;
        multiLineHeight += countLabelMultiLineHeight(pdfField);
        if (pdfField.getValue() != null) {
            multiLineHeight += countValueMultiLineHeight(pdfField);
        }
        return multiLineHeight;
    }

    private int countLabelMultiLineHeight(PdfField<T> pdfField) {
        return pdfField.getLabel().size() *  resource.getLineHeight() + resource.getPadding();
    }

    private int countFieldLayoutX(DataGroup dataGroup, DataRef field) {
        int x = 0;
        if (isDgFlow(dataGroup)) {
            if (!isStretch(dataGroup)) {
                x = lastX < resource.getFormGridCols() ? lastX : 0;
            }
            lastX = x;
        } else if (isDgLegacy(dataGroup)) {
            if (isStretch(dataGroup)) {
                lastX = 0;
            } else {
                lastX = lastX == 0 ? 2 : 0;
            }
            x = lastX;
        } else if (field.getLayout() != null) {
            x = field.getLayout().getX();
            lastX = x;
        }
        return x;
    }

    private int countFieldLayoutY(DataGroup dataGroup, DataRef field) {
        int y;
        if (checkFullRow(dataGroup, field)) {
            y = ++lastY;
            resolveRowGridFree(dataGroup, field.getLayout());
        } else {
            if (lastX == 0) {
                y = ++lastY;
                resolveRowGridFree(dataGroup, field.getLayout());
            } else {
                y = lastY;
                resource.setRowGridFree(!checkCol(field.getLayout()) ? 2 : resource.getRowGridFree() - field.getLayout().getCols());
            }
            if (isDgFlow(dataGroup)) {
                lastX++;
            }
        }
        return y;
    }

    private int countPosX(PdfField<T> field) {
        return (field.getLayoutX() * resource.getFormGridColWidth() + resource.getPadding());
    }

}
