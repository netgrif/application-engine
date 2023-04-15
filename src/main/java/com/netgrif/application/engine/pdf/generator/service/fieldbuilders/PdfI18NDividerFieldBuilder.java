package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfI18nDividerField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;

import java.util.Collections;
import java.util.List;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

public class PdfI18NDividerFieldBuilder extends PdfFormFieldBuilder<PdfI18nDividerField> {

    public PdfI18NDividerFieldBuilder() {
        super();
    }

    @Override
    public PdfI18nDividerField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfI18nDividerField pdfField = new PdfI18nDividerField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.I_18_N.value();
    }

    @Override
    public int countValueMultiLineHeight(PdfI18nDividerField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfI18nDividerField pdfField) {
        I18nField field = (I18nField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String rawValue = field.getValue() != null ? field.getValue().getValue().getTranslation(buildingBlock.getLocale()) : "";
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        List<String> value = generateMultiLineText(Collections.singletonList(rawValue), maxValueLineLength);
        pdfField.setValue(value);
    }
}
