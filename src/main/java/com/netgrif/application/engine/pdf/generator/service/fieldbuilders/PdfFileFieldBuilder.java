package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfFileField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfFileFieldBuilder extends PdfFormFieldBuilder<PdfFileField> {

    @Override
    public PdfFileField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfFormFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return DataType.FILE.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfFileField pdfField) {
        setupValue((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    public int countValueMultiLineHeight(PdfFileField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    private PdfFileField buildField(PdfFormFieldBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfFileField pdfField = new PdfFileField(buildingBlock.getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    private void setupValue(PdfFormFieldBuildingBlock buildingBlock, PdfFileField pdfField) {
        Field<?> field = buildingBlock.getDataRef().getField();
        String rawValue = field.getValue() != null ? shortenFileName(((FileFieldValue) field.getValue().getValue()).getName()) : "";
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        List<String> value = generateMultiLineText(Collections.singletonList(rawValue), maxValueLineLength);
        pdfField.setValue(value);
    }

    private String shortenFileName(String fileName) {
        if (fileName.length() > 32) {
            return fileName.substring(0, 16) + "..." + fileName.substring(fileName.length() - 8);
        }
        return fileName;
    }
}