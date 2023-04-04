package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfSelectionField;
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
public class PdfListMultiChoiceFieldBuilder extends PdfMultiChoiceFieldBuilder {

    public PdfListMultiChoiceFieldBuilder() {
        super();
    }

    @Override
    public String getType() {
        return DataType.MULTICHOICE.value() + "_" + PdfSelectionField.LIST_COMPONENT_NAME;
    }

    @Override
    protected int countValueMultiLineHeight(PdfMultiChoiceField pdfField) {
        return (int) pdfField.getValue().values().stream().mapToLong(List::size).sum() * resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfMultiChoiceField pdfField) {
        MultichoiceField field = (MultichoiceField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
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
            Set<String> values = field.getValue().getValue().stream().map(I18nString::getDefaultValue).collect(Collectors.toSet());
            pdfField.setSelectedValues(values);
        }
    }
}
