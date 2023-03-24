package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfDataGroupField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfDataGroupFieldBuildingBlock;
import com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfDataGroupFieldBuilder extends PdfFieldBuilder<PdfDataGroupField> {

    public PdfDataGroupFieldBuilder() {
        super();
    }

    @Override
    public PdfDataGroupField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfDataGroupFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return PdfDataGroupField.DATA_GROUP_TYPE;
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfDataGroupField pdfField) {
        setupValue((PdfDataGroupFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    protected void setFieldParams(PdfBuildingBlock buildingBlock, PdfDataGroupField pdfField) {
        setFieldParams((PdfDataGroupFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    protected int countValueMultiLineHeight(PdfDataGroupField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    private PdfDataGroupField buildField(PdfDataGroupFieldBuildingBlock buildingBlock) {
        PdfDataGroupField dataGroupField = new PdfDataGroupField(buildingBlock.getImportId());
        setFieldParams(buildingBlock, dataGroupField);
        setFieldPositions(dataGroupField);
        return dataGroupField;
    }

    private void setupValue(PdfDataGroupFieldBuildingBlock buildingBlock, PdfDataGroupField pdfField) {
        int maxLineLength = getMaxLineSize(
                resource.getPageDrawableWidth(),
                resource.getFontTitleSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        String translatedTitle = buildingBlock.getTitle().getTranslation(buildingBlock.getLocale());
        pdfField.setValue(PdfGeneratorUtils.generateMultiLineText(Arrays.asList(StringUtils.EMPTY, translatedTitle), maxLineLength));
    }

    private void setFieldParams(PdfDataGroupFieldBuildingBlock buildingBlock, PdfDataGroupField pdfField) {
        lastY = buildingBlock.getLastY();
        if (buildingBlock.getLastX() != 0) {
            lastY++;
        }
        pdfField.setLayoutX(0);
        pdfField.setLayoutY(lastY);
        pdfField.setWidth(resource.getPageDrawableWidth());
        setupValue(buildingBlock, pdfField);
        pdfField.setHeight(countFieldHeight());
    }
//    @Override
//    public PdfField buildField(PdfFieldBuildingBlock buildingBlock) {
//        return buildField(buildingBlock.getDataGroup());
//    }
//
//    @Override
//    public DataType getType() {
//        return null;
//    }
//
//    public PdfField buildField(DataGroup dataGroup, PdfField pdfField) {
//        PdfField dgField = new PdfDataGroupField(dataGroup.getImportId(), 0, pdfField.getLayoutY(),
//                resource.getPageDrawableWidth(), resource.getFormGridRowHeight() - resource.getPadding(), dataGroup.getTitle().toString(), true, resource);
//        setFieldPositions(dgField, resource.getFontGroupSize());
//        return dgField;
//    }
}
