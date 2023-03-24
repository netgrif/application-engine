package com.netgrif.application.engine.pdf.generator.service.fieldbuilders;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.pdf.generator.domain.fields.PdfTextField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfBuildingBlock;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.PdfFormFieldBuildingBlock;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.generateMultiLineText;
import static com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils.getMaxLineSize;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PdfTextFieldBuilder extends PdfFormFieldBuilder<PdfTextField> {

    public PdfTextFieldBuilder() {
        super();
    }

    @Override
    public PdfTextField buildField(PdfBuildingBlock buildingBlock) {
        return buildField((PdfFormFieldBuildingBlock) buildingBlock);
    }

    @Override
    public String getType() {
        return DataType.TEXT.value();
    }

    @Override
    public int countValueMultiLineHeight(PdfTextField pdfField) {
        return pdfField.getValue().size() * resource.getLineHeight() + resource.getPadding();
    }

    @Override
    protected void setupValue(PdfBuildingBlock buildingBlock, PdfTextField pdfField) {
        setupValue((PdfFormFieldBuildingBlock) buildingBlock, pdfField);
    }

    private PdfTextField buildField(PdfFormFieldBuildingBlock buildingBlock) {
        this.lastX = buildingBlock.getLastX();
        this.lastY = buildingBlock.getLastY();
        PdfTextField pdfField = new PdfTextField(buildingBlock.getDataRef().getField().getStringId());
        setFieldParams(buildingBlock, pdfField);
        setFieldPositions(pdfField);
        return pdfField;
    }

    private void setupValue(PdfFormFieldBuildingBlock buildingBlock, PdfTextField pdfField) {
        Field<?> field = buildingBlock.getDataRef().getField();
        String rawValue = field.getValue() != null ? Jsoup.parse(field.getValue().toString()).text() : "";
        int maxValueLineLength = getMaxLineSize(
                pdfField.getWidth() - 3 * resource.getPadding(),
                resource.getFontValueSize(),
                resource.getPadding(),
                resource.getSizeMultiplier()
        );
        List<String> value = generateMultiLineText(Collections.singletonList(rawValue), maxValueLineLength);
        pdfField.setValue(value);
    }

//    public PdfField buildField(DataGroup dataGroup, DataRef dataRef, int lastX, int lastY, Locale locale) {
//        switch (field.getType()) {
//            case DATE:
//                value = field.getValue() != null ? formatDate(field) : "";
//                break;
//            case DATE_TIME:
//                value = field.getValue() != null ? formatDateTime(field) : "";
//                break;
//            case NUMBER:
//                double number = field.getValue() != null ? (double) field.getValue().getValue() : 0.0;
//                if (field.getValue() != null && isCurrencyField(field)) {
//                    Map<String, String> properties = field.getComponent().getProperties();
//                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale(properties.get("locale")));
//                    currencyFormat.setCurrency(Currency.getInstance(properties.get("code")));
//                    currencyFormat.setMaximumFractionDigits(Integer.parseInt(properties.get("fractionSize")));
//                    value = currencyFormat.format(number);
//                } else if (field.getValue() != null) {
//                    NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
//                    value = nf2.format(number);
//                } else {
//                    value = "";
//                }
//                break;
//            case FILE:
//                value = field.getValue() != null ? shortenFileName(((FileFieldValue) field.getValue().getValue()).getName()) : "";
//                break;
//            case FILE_LIST:
//                value = field.getValue() != null ? resolveFileListNames((FileListFieldValue) field.getValue().getValue()) : "";
//                break;
//            case USER:
//                value = field.getValue() != null ? ((UserFieldValue) field.getValue().getValue()).getFullName() : "";
//                break;
//            case USER_LIST:
//                value = field.getValue() != null ? ((UserListFieldValue) field.getValue().getValue()).getUserValues().stream().map(UserFieldValue::getFullName).collect(Collectors.joining(", ")) : "";
//                break;
//            default:
//                value = field.getValue() != null ? Jsoup.parse(field.getValue().toString()).text() : "";
//                break;
//        }
//        return null;
//    }



//    private String formatDateTime(Field<?> field) {
//        ZonedDateTime value = ZonedDateTime.now();
//        if (field.getValue() != null) {
//            if (field.getValue().getValue() instanceof LocalDateTime)
//                value = DateUtils.localDateTimeToZonedDateTime((LocalDateTime) field.getValue().getValue(), resource.getDateZoneId());
//            else if (field.getValue().getValue() instanceof Date)
//                value = ((Date) field.getValue().getValue()).toInstant().atZone(resource.getDateZoneId());
//            return DateTimeFormatter.ofPattern(resource.getDateTimeFormat().getValue()).format(value);
//        } else {
//            return StringUtils.EMPTY;
//        }
//    }

//    private String resolveFileListNames(FileListFieldValue files) {
//        return files.getNamesPaths().stream()
//                .map(it -> shortenFileName(it.getName()))
//                .collect(Collectors.joining(", "));
//    }
//
//    private String shortenFileName(String fileName) {
//        if (fileName.length() > 32) {
//            return fileName.substring(0, 16) + "..." + fileName.substring(fileName.length() - 8);
//        }
//        return fileName;
//    }

//    private boolean isCurrencyField(Field<?> field) {
//        return field.getComponent() != null &&
//                Objects.equals(field.getComponent().getName(), "currency") &&
//                field.getComponent().getProperties() != null &&
//                field.getComponent().getProperties().containsKey("code") &&
//                field.getComponent().getProperties().get("code") != null &&
//                field.getComponent().getProperties().containsKey("locale") &&
//                field.getComponent().getProperties().get("locale") != null &&
//                field.getComponent().getProperties().containsKey("fractionSize") &&
//                field.getComponent().getProperties().get("fractionSize") != null;
//    }
}
