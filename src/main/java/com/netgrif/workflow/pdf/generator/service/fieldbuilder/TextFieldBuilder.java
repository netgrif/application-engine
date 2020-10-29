package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfTextField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.utils.DateUtils;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class TextFieldBuilder extends FieldBuilder{

    public TextFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedField field, int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
        String value;
        switch (field.getType()) {
            case DATE:
                value =  field.getValue() != null ? formatDate(field) : "";
                break;
            case DATETIME:
                value =  field.getValue() != null ? formatDateTime(field) : "";
                break;
            case NUMBER:
                double number = field.getValue() != null ? (double) field.getValue() : 0.0;
                NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
                value = nf2.format(number);
                break;
            case FILE:
                value = field.getValue() != null ? ((FileFieldValue)field.getValue()).getName() : "";
                break;
            default:
                value = field.getValue() != null ? field.getValue().toString() : "";
                break;
        }
        String translatedTitle = field.getName();
        PdfField pdfField = new PdfTextField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    private String formatDate(LocalisedField field) {
        Date value = new Date();
        if (field.getValue() != null) {
            if(field.getValue() instanceof LocalDate)
                value = DateUtils.localDateToDate((LocalDate) field.getValue());
            else if(field.getValue() instanceof Date)
                value = (Date) field.getValue();
            return new SimpleDateFormat(resource.getDateFormat().getValue()).format(value);
        } else {
            return "";
        }
    }

    private String formatDateTime(LocalisedField field) {
        Date value = new Date();
        if (field.getValue() != null) {
            if(field.getValue() instanceof LocalDateTime)
                value = DateUtils.localDateTimeToDate((LocalDateTime) field.getValue());
            else if(field.getValue() instanceof Date)
                value = (Date) field.getValue();
            return new SimpleDateFormat(resource.getDateTimeFormat().getValue()).format(value);
        } else {
            return "";
        }
    }
}
