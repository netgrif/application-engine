package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.utils.DateUtils;
import org.jsoup.Jsoup;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TextFieldBuilder extends FieldBuilder {

    public TextFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, Field field, int lastX, int lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
        String value;
        switch (field.getType()) {
            case DATE:
                value = field.getValue() != null ? formatDate(field) : "";
                break;
            case DATE_TIME:
                value = field.getValue() != null ? formatDateTime(field) : "";
                break;
            case NUMBER:
                double number = field.getValue() != null ? (double) field.getValue() : 0.0;
                NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
                value = nf2.format(number);
                break;
            case FILE:
                value = field.getValue() != null ? shortenFileName(((FileFieldValue) field.getValue()).getName()) : "";
                break;
            case FILE_LIST:
                value = field.getValue() != null ? resolveFileListNames((FileListFieldValue) field.getValue()) : "";
                break;
            case USER:
                value = field.getValue() != null ? ((UserFieldValue) field.getValue()).getFullName() : "";
                break;
            default:
                value = field.getValue() != null ? Jsoup.parse(field.getValue().toString()).text() : "";
                break;
        }
        String translatedTitle = field.getName().getTranslation(LocaleContextHolder.getLocale());
        PdfField pdfField = new PdfTextField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    private String formatDate(Field field) {
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

    private String formatDateTime(Field field) {
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
        if (fileName.length() > 24) {
            return fileName.substring(24, fileName.length() - 1);
        }
        return fileName;
    }
}
