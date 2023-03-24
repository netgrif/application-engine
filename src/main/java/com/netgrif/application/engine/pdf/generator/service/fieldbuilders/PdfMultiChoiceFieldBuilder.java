package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfMultiChoiceFieldBuilder extends PdfFormFieldBuilder<PdfMultiChoiceField> {

    public PdfMultiChoiceFieldBuilder() {
        super();
    }

    @Override
    public PdfMultiChoiceField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfFormFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return DataType.MULTICHOICE.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfMultiChoiceField pdfField) {
        setupValue((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    protected int countValueMultiLineHeight(PdfMultiChoiceField pdfField) {
        return (int) pdfField.getValue().values().stream().mapToLong(List::size).sum() * resource.getLineHeight() + resource.getPadding();
    }

    private void setupValue(PdfFormFieldBuildingBlock buildingBlock, PdfMultiChoiceField pdfField) {
        MultichoiceField field = (MultichoiceField) buildingBlock.getDataRef().getField();
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
            Set<String> values = field.getValue().getValue().stream().map(I18nString::getKey).collect(Collectors.toSet());
            pdfField.setSelectedValues(values);
        }
    }

    private PdfMultiChoiceField buildField(PdfFormFieldBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(buildingBlock.getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }
}
