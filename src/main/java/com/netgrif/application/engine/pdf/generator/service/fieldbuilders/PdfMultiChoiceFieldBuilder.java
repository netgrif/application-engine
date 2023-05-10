package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfMultiChoiceField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String[] getType() {
        return new String[]{DataType.MULTICHOICE.value()};
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
        if (field.getValue() != null && field.getValue().getValue() != null) {
            Set<String> values = new HashSet<>(generateMultiLineText(Collections.singletonList(field.getValue().getValue().stream().map(v -> v.getTranslation(buildingBlock.getLocale())).collect(Collectors.joining(", "))), maxValueLineLength));
            pdfField.setSelectedValues(values);
        }
    }

    @Override
    protected int countValueMultiLineHeight(PdfMultiChoiceField pdfField) {
        return pdfField.getSelectedValues().size() * resource.getLineHeight() + resource.getPadding();
    }
}
