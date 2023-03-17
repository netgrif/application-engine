package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.utils.DateUtils;
import org.jsoup.Jsoup;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TextFieldBuilder extends FieldBuilder {

    public TextFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, DataRef dataRef, int lastX, int lastY, Locale locale) {
        this.lastX = lastX;
        this.lastY = lastY;
        String value;
        Field<?> field = dataRef.getField();
        switch (field.getType()) {
            case DATE:
                value = field.getValue() != null ? formatDate(field) : "";
                break;
            case DATE_TIME:
                value = field.getValue() != null ? formatDateTime(field) : "";
                break;
            case NUMBER:
                double number = field.getValue() != null ? (double) field.getValue().getValue() : 0.0;
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
                break;
            case FILE:
                value = field.getValue() != null ? shortenFileName(((FileFieldValue) field.getValue().getValue()).getName()) : "";
                break;
            case FILE_LIST:
                value = field.getValue() != null ? resolveFileListNames((FileListFieldValue) field.getValue().getValue()) : "";
                break;
            case USER:
                value = field.getValue() != null ? ((UserFieldValue) field.getValue().getValue()).getFullName() : "";
                break;
            case USER_LIST:
                value = field.getValue() != null ? ((UserListFieldValue) field.getValue().getValue()).getUserValues().stream().map(UserFieldValue::getFullName).collect(Collectors.joining(", ")) : "";
                break;
            default:
                value = field.getValue() != null ? Jsoup.parse(field.getValue().toString()).text() : "";
                break;
        }
        String translatedTitle = field.getName().getTranslation(locale);
        PdfField pdfField = new PdfTextField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, dataRef, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    public PdfField buildField(DataGroup dataGroup, DataRef field, int lastX, int lastY) {
        return buildField(dataGroup, field, lastX, lastY, LocaleContextHolder.getLocale());
    }

    private String formatDate(Field<?> field) {
        ZonedDateTime value = ZonedDateTime.now();
        if (field.getValue() != null) {
            // TODO: release/7.0.0 not needed anymore, fixed class of value
            if (field.getValue().getValue() instanceof LocalDate)
                value = DateUtils.localDateToZonedDate((LocalDate) field.getValue().getValue(), resource.getDateZoneId());
            else if (field.getValue().getValue() instanceof Date)
                value = ((Date) field.getValue().getValue()).toInstant().atZone(resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateFormat().getValue()).format(value);
        } else {
            return "";
        }
    }

    private String formatDateTime(Field<?> field) {
        ZonedDateTime value = ZonedDateTime.now();
        if (field.getValue() != null) {
            if (field.getValue().getValue() instanceof LocalDateTime)
                value = DateUtils.localDateTimeToZonedDateTime((LocalDateTime) field.getValue().getValue(), resource.getDateZoneId());
            else if (field.getValue().getValue() instanceof Date)
                value = ((Date) field.getValue().getValue()).toInstant().atZone(resource.getDateZoneId());
            return DateTimeFormatter.ofPattern(resource.getDateTimeFormat().getValue()).format(value);
        } else {
            return "";
        }
    }

    private String resolveFileListNames(FileListFieldValue files) {
        return files.getNamesPaths().stream()
                .map(it -> shortenFileName(it.getName()))
                .collect(Collectors.joining(", "));
    }

    private String shortenFileName(String fileName) {
        if (fileName.length() > 32) {
            return fileName.substring(0, 16) + "..." + fileName.substring(fileName.length() - 8);
        }
        return fileName;
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
