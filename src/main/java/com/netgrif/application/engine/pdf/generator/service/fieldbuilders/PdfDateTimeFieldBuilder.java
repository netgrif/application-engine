package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfDateTimeField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
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
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfDateTimeField pdfField = new PdfDateTimeField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String[] getType() {
        return new String[]{DataType.DATE_TIME.value()};
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfDateTimeField pdfField) {
        DateTimeField field = (DateTimeField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String value = field.getValue() != null ? formatDateTime(field) : StringUtils.EMPTY;
        pdfField.setValue(value);
    }

    @Override
    protected int countValueMultiLineHeight(PdfDateTimeField pdfField) {
        return resource.getLineHeight() + resource.getPadding();
    }

    private String formatDateTime(Field<?> field) {
        if (field.getValue().getValue() != null) {
            ZonedDateTime value = DateUtils.localDateTimeToZonedDateTime((LocalDateTime) field.getValue().getValue(), resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateTimeFormat().getValue()).format(value);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
