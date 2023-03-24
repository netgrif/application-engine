package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfDateTimeField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.utils.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope((ConfigurableBeanFactory.SCOPE_PROTOTYPE))
public class PdfDateTimeFieldBuilder extends PdfFormFieldBuilder<PdfDateTimeField> {

    @Override
    public PdfDateTimeField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfFormFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return DataType.DATE_TIME.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfDateTimeField pdfField) {
        setupValue((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    @Override
    protected int countValueMultiLineHeight(PdfDateTimeField pdfField) {
        return resource.getLineHeight() + resource.getPadding();
    }

    private PdfDateTimeField buildField(PdfFormFieldBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfDateTimeField pdfField = new PdfDateTimeField(buildingBlock.getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    private void setupValue(PdfFormFieldBuildingBlock buildingBlock, PdfDateTimeField pdfField) {
        Field<?> field = buildingBlock.getDataRef().getField();
        String value = field.getValue() != null ? formatDateTime(field) : StringUtils.EMPTY;
        pdfField.setValue(value);
    }

    private String formatDateTime(Field<?> field) {
        if (field.getValue() != null) {
            ZonedDateTime value = DateUtils.localDateTimeToZonedDateTime((LocalDateTime) field.getValue().getValue(), resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateTimeFormat().getValue()).format(value);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
