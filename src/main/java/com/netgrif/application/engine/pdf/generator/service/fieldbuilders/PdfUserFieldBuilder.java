package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfUserField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope((ConfigurableBeanFactory.SCOPE_PROTOTYPE))
public class PdfUserFieldBuilder extends PdfFormFieldBuilder<PdfUserField> {

    @Override
    public PdfUserField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfUserField pdfField = new PdfUserField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.USER.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfUserField pdfField) {
        Field<?> field = ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String value = field.getValue() != null ? ((UserFieldValue) field.getValue().getValue()).getFullName() : "";
        pdfField.setValue(value);
    }

    @Override
    protected int countValueMultiLineHeight(PdfUserField pdfField) {
        return resource.getLineHeight() + resource.getPadding();
    }
}
