package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.config.types.PdfBooleanFormat;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfBooleanField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfBooleanFieldBuilder extends PdfFormFieldBuilder<PdfBooleanField> {

    public PdfBooleanFieldBuilder() {
        super();
    }

    @Override
    public PdfBooleanField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfBooleanField pdfField = new PdfBooleanField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String[] getType() {
        return new String[]{DataType.BOOLEAN.value()};
    }

    @Override
    public int countValueMultiLineHeight(PdfBooleanField pdfField) {
        return resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfBooleanField pdfField) {
        BooleanField field = (BooleanField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        pdfField.setFormat(PdfBooleanFormat.getByLocale(buildingBlock.getLocale()));
        pdfField.setValue(field.getValue().getValue() != null && field.getValue().getValue());
    }
}
