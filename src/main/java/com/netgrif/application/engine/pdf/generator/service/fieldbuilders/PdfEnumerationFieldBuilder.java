package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfEnumerationField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfEnumerationFieldBuilder extends PdfFormFieldBuilder<PdfEnumerationField> {

    public PdfEnumerationFieldBuilder() {
        super();
    }

    @Override
    public PdfEnumerationField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfFormFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return DataType.ENUMERATION.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfEnumerationField pdfField) {
        setupValue((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    protected int countValueMultiLineHeight(PdfEnumerationField pdfField) {
        return (int) pdfField.getValue().values().stream().mapToLong(List::size).sum() * resource.getLineHeight() + resource.getPadding();
    }

    private PdfEnumerationField buildField(PdfFormFieldBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfEnumerationField pdfField = new PdfEnumerationField(buildingBlock.getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    private void setupValue(PdfFormFieldBuildingBlock buildingBlock, PdfEnumerationField pdfField) {
        EnumerationField field = (EnumerationField) buildingBlock.getDataRef().getField();
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        if (field.getChoices() != null) {
            Map<String, List<String>> choices = field.getChoices().stream().collect(Collectors.toMap(I18nString::getKey, e -> generateMultiLineText(Collections.singletonList(e.getTranslation(buildingBlock.getLocale())), maxValueLineLength)));
            pdfField.setValue(choices);
        }
        if (field.getValue() != null) {
            String values = field.getValue().getValue().getKey();
            pdfField.setSelectedValues(values);
        }
    }
}
