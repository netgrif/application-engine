package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfEnumerationMapField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfEnumerationMapFieldBuilder extends PdfFormFieldBuilder<PdfEnumerationMapField> {

    public PdfEnumerationMapFieldBuilder() {
        super();
    }

    @Override
    public PdfEnumerationMapField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfFormFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return DataType.ENUMERATION_MAP.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfEnumerationMapField pdfField) {
        setupValue((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    protected int countValueMultiLineHeight(PdfEnumerationMapField pdfField) {
        return 0;
    }

    private PdfEnumerationMapField buildField(PdfFormFieldBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfEnumerationMapField pdfField = new PdfEnumerationMapField(buildingBlock.getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    private void setupValue(PdfFormFieldBuildingBlock buildingBlock, PdfEnumerationMapField pdfField) {
        EnumerationMapField field = (EnumerationMapField) buildingBlock.getDataRef().getField();
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        if (field.getOptions() != null) {
            Map<String, List<String>> choices = field.getOptions().entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getKey(), e -> generateMultiLineText(Collections.singletonList(e.getValue().getTranslation(buildingBlock.getLocale())), maxValueLineLength)));
            pdfField.setValue(choices);
        }
        if (field.getValue() != null) {
            String values = field.getValue().getValue();
            pdfField.setSelectedValues(values);
        }
    }
}
