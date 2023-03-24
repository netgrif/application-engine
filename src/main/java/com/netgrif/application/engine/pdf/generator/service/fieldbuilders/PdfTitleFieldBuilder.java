package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTitleField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfTitleFieldBuildingBlock;
import com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfTitleFieldBuilder extends PdfFieldBuilder<PdfTitleField> {

    public PdfTitleFieldBuilder() {
        super();
    }

    @Override
    public PdfTitleField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfTitleFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return PdfTitleField.TITLE_TYPE;
    }

    @Override
    protected void setFieldParams(PdfBuildingBlock buildingBlock, PdfTitleField pdfField) {
        pdfField.setLayoutX(0);
        pdfField.setLayoutY(0);
        pdfField.setWidth(resource.getPageDrawableWidth());
        setupValue(buildingBlock, pdfField);
        pdfField.setHeight(countMultiLineHeight(pdfField));
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfTitleField pdfField) {
        setupValue((PdfTitleFieldBuildingBlock)  buildingBlock, pdfField);
    }

    protected void setupValue(PdfTitleFieldBuildingBlock buildingBlock, PdfTitleField pdfField) {
        int maxLineLength = getMaxLineSize(
                resource.getPageDrawableWidth(),
                resource.getFontTitleSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        pdfField.setValue(PdfGeneratorUtils.generateMultiLineText(Collections.singletonList(buildingBlock.getText()), maxLineLength));
    }

    @Override
    protected int countValueMultiLineHeight(PdfTitleField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    private PdfTitleField buildField(PdfTitleFieldBuildingBlock buildingBlock) {
        PdfTitleField titleField = new PdfTitleField("titleField");
        setFieldParams(buildingBlock, titleField);
        return titleField;
    }
}
