package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfEnumerationMapField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationField;
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
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfEnumerationMapField pdfField = new PdfEnumerationMapField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.ENUMERATION_MAP.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfEnumerationMapField pdfField) {
        EnumerationField field = (EnumerationField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        if (field.getValue() != null) {
            Set<String> values = new HashSet<>(generateMultiLineText(Collections.singletonList(field.getValue().getValue().getTranslation(buildingBlock.getLocale())), maxValueLineLength));
            pdfField.setSelectedValues(values);
        }
    }

    @Override
    protected int countValueMultiLineHeight(PdfEnumerationMapField pdfField) {
        return pdfField.getSelectedValues().size() * resource.getLineHeight() + resource.getPadding();
    }
}
