package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfDateField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.DateField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.utils.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfDateFieldBuilder extends PdfFormFieldBuilder<PdfDateField> {

    @Override
    public PdfDateField buildField(PdfBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfDateField pdfField = new PdfDateField(((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    @Override
    public String getType() {
        return DataType.DATE.value();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfDateField pdfField) {
        DateField field = (DateField) ((PdfFormFieldBuildingBlock) buildingBlock).getDataRef().getField();
        String value = field.getValue() != null ? formatDate(field) : StringUtils.EMPTY;
        pdfField.setValue(value);
    }

    @Override
    protected int countValueMultiLineHeight(PdfDateField pdfField) {
        return resource.getLineHeight() + resource.getPadding();
    }

    private String formatDate(Field<?> field) {
        if (field.getValue() != null) {
            ZonedDateTime value = DateUtils.localDateToZonedDate((LocalDate) field.getValue().getValue(), resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateFormat().getValue()).format(value);
        } else {
            return StringUtils.EMPTY;
        }
    }
}