package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfNumberField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfNumberFieldBuilder extends PdfFormFieldBuilder<PdfNumberField> {

    @Override
    public PdfNumberField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfNumberField pdfField = new PdfNumberField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.NUMBER.value();
    }

    @Override
    public int countValueMultiLineHeight(PdfNumberField pdfField) {
        return resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfNumberField pdfField) {
        NumberField field = (NumberField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String value;
        double number = field.getValue() != null ? field.getValue().getValue() : 0.0;
        if (field.getValue() != null && isCurrencyField(field)) {
            Map<String, String> properties = field.getComponent().getProperties();
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale(properties.get("locale")));
            currencyFormat.setCurrency(Currency.getInstance(properties.get("code")));
            currencyFormat.setMaximumFractionDigits(Integer.parseInt(properties.get("fractionSize")));
            value = currencyFormat.format(number);
        } else if (field.getValue() != null) {
            NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
            value = nf2.format(number);
        } else {
            value = "";
        }
        pdfField.setValue(value);
    }

    private boolean isCurrencyField(Field<?> field) {
        return field.getComponent() != null &&
                Objects.equals(field.getComponent().getName(), "currency") &&
                field.getComponent().getProperties() != null &&
                field.getComponent().getProperties().containsKey("code") &&
                field.getComponent().getProperties().get("code") != null &&
                field.getComponent().getProperties().containsKey("locale") &&
                field.getComponent().getProperties().get("locale") != null &&
                field.getComponent().getProperties().containsKey("fractionSize") &&
                field.getComponent().getProperties().get("fractionSize") != null;
    }
}
