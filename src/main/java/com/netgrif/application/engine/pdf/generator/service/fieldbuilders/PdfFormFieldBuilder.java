package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.LayoutType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class PdfFormFieldBuilder<T extends PdfField<?>> extends PdfFieldBuilder<T> {

    public PdfFormFieldBuilder() {
        super();
    }

    @Override
    protected void setFieldParams(PdfBuildingBlock buildingBlock, T pdfField) {
        setFieldParams((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    protected void setFieldParams(PdfFormFieldBuildingBlock buildingBlock, T pdfField) {
        pdfField.setLayoutX(countFieldLayoutX(buildingBlock.getDataGroup(), buildingBlock.getDataRef()));
        pdfField.setLayoutY(countFieldLayoutY(buildingBlock.getDataGroup(), buildingBlock.getDataRef()));
        pdfField.setWidth(countFieldWidth(buildingBlock.getDataGroup(), buildingBlock.getDataRef()));
        setupLabel(buildingBlock, pdfField);
        setupValue(buildingBlock, pdfField);
        pdfField.setHeight(countFieldHeight());
    }

    private void setupLabel(PdfFormFieldBuildingBlock buildingBlock, T pdfField) {
        String translatedTitle = buildingBlock.getDataRef().getField().getName().getTranslation(buildingBlock.getLocale());
        int maxLabelLineLength = getMaxLineSize(pdfField.getWidth(), resource.getFontLabelSize(), resource.getPadding(), resource.getSizeMultiplier());
        List<String> label = generateMultiLineText(Collections.singletonList(translatedTitle), maxLabelLineLength);
        pdfField.setLabel(label);
    }

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
}
