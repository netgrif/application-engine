package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfTextFieldBuilder extends PdfFormFieldBuilder<PdfTextField> {

    public PdfTextFieldBuilder() {
        super();
    }

    @Override
    public PdfTextField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfTextField pdfField = new PdfTextField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String[] getType() {
        return new String[]{
                DataType.TEXT.value(),
                DataType.TEXT.value() + "_" + "password",
                DataType.TEXT.value() + "_" + "textarea",
                DataType.TEXT.value() + "_" + "area",
                DataType.TEXT.value() + "_" + "richtextarea",
                DataType.TEXT.value() + "_" + "editor",
                DataType.TEXT.value() + "_" + "htmltextarea",
                DataType.TEXT.value() + "_" + "htmlEditor"
        };
    }

    @Override
    public int countValueMultiLineHeight(PdfTextField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfTextField pdfField) {
        TextField field = (TextField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String rawValue = field.getValue() != null ? Jsoup.parse(field.getValue().toString()).text() : "";
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
