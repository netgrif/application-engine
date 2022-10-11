package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedField;
import org.jsoup.Jsoup;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.stream.Collectors;

public class TextFieldBuilder extends FieldBuilder {

    public TextFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedField field, int lastX, int lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
        String value;
        switch (field.getType()) {
            case DATE:
                value = field.getValue() != null ? formatDate(field) : "";
                break;
            case DATETIME:
                value = field.getValue() != null ? formatDateTime(field) : "";
                break;
            case NUMBER:
                if (field.getValue() != null) {
                    double number = (double) field.getValue();
                    NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
                    value = nf2.format(number);
                } else {
                    value = "";
                }
                break;
            case FILE:
                value = field.getValue() != null ? shortenFileName(((FileFieldValue) field.getValue()).getName()) : "";
                break;
            case FILELIST:
                value = field.getValue() != null ? resolveFileListNames((FileListFieldValue) field.getValue()) : "";
                break;
            case USER:
                value = field.getValue() != null ? ((UserFieldValue) field.getValue()).getFullName() : "";
                break;
            case USERLIST:
                value = field.getValue() != null ? ((UserListFieldValue) field.getValue()).getUserValues().stream().map(UserFieldValue::getFullName).collect(Collectors.joining(", ")) : "";
                break;
            default:
                value = field.getValue() != null ? Jsoup.parse(field.getValue().toString()).text() : "";
                break;
        }
        String translatedTitle = field.getName();
        PdfField pdfField = new PdfTextField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    private String formatDate(LocalisedField field) {
        ZonedDateTime value = ZonedDateTime.now();
        if (field.getValue() != null) {
            if (field.getValue() instanceof LocalDate)
                value = DateUtils.localDateToZonedDate((LocalDate) field.getValue(), resource.getDateZoneId());
            else if (field.getValue() instanceof Date)
                value = ((Date) field.getValue()).toInstant().atZone(resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateFormat().getValue()).format(value);
        } else {
            return "";
        }
    }

    private String formatDateTime(LocalisedField field) {
        ZonedDateTime value = ZonedDateTime.now();
        if (field.getValue() != null) {
            if (field.getValue() instanceof LocalDateTime)
                value = DateUtils.localDateTimeToZonedDateTime((LocalDateTime) field.getValue(), resource.getDateZoneId());
            else if (field.getValue() instanceof Date)
                value = ((Date) field.getValue()).toInstant().atZone(resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateTimeFormat().getValue()).format(value);
        } else {
            return "";
        }
    }

    private String resolveFileListNames(FileListFieldValue files) {
        StringBuilder builder = new StringBuilder();

        files.getNamesPaths().forEach(value -> {
            builder.append(shortenFileName(value.getName()));
            builder.append(", ");
        });

        return builder.toString();
    }

    private String shortenFileName(String fileName) {
        if (fileName.length() > 32) {
            return fileName.substring(0, 16) + "..." + fileName.substring(fileName.length() - 8);
        }
        return fileName;
    }
}
