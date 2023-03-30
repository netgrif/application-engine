package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceMapField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfSelectionField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfListMultiChoiceMapFieldBuilder extends PdfMultiChoiceMapFieldBuilder {

    @Override
    public String getType() {
        return DataType.MULTICHOICE_MAP.value() + "_" + PdfSelectionField.LIST_COMPONENT_NAME;
    }

    @Override
    protected int countValueMultiLineHeight(PdfMultiChoiceMapField pdfField) {
        return (int) pdfField.getValue().values().stream().mapToLong(List::size).sum() * resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfMultiChoiceMapField pdfField) {
        MultichoiceMapField field = (MultichoiceMapField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
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
            Set<String> values = field.getValue().getValue();
            pdfField.setSelectedValues(values);
        }
    }
}
