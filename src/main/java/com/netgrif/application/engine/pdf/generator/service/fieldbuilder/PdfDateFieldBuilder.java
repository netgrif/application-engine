//package com.netgrif.application.engine.pdf.generator.service.fieldbuilder;
//
//import com.netgrif.application.engine.importer.model.DataType;
//import com.netgrif.application.engine.pdf.generator.config.PdfResource;
//import com.netgrif.application.engine.pdf.generator.domain.PdfField;
//import com.netgrif.application.engine.pdf.generator.domain.PdfTextField;
//import com.netgrif.application.engine.petrinet.domain.dataset.Field;
//import com.netgrif.application.engine.utils.DateUtils;
//import org.jsoup.Jsoup;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//
//@Component
//public class PdfDateFieldBuilder extends PdfFieldBuilder {
//
//    public PdfDateFieldBuilder(PdfResource pdfResource) {
//        super(pdfResource);
//    }
//    @Override
//    public PdfField buildField(PdfFieldBuildingBlock buildingBlock) {
//        this.lastX = buildingBlock.getLastX();
//        this.lastY = buildingBlock.getLastY();
//        Field<?> field = buildingBlock.getDataRef().getField();
//        String value = field.getValue() != null ? formatDate(field) : "";
//        String translatedTitle = field.getName().getTranslation(buildingBlock.getLocale());
//        PdfField pdfField = new PdfTextField(field.getStringId(), buildingBlock.getDataGroup(), field.getType(), translatedTitle, value, resource);
//        setFieldParams(buildingBlock.getDataGroup(), buildingBlock.getDataRef(), pdfField);
//        setFieldPositions(pdfField, resource.getFontLabelSize());
//        return pdfField;
//    }
//
//    @Override
//    public DataType getType() {
//        return DataType.DATE;
//    }
//
//    private String formatDate(Field<?> field) {
//        ZonedDateTime value = ZonedDateTime.now();
//        if (field.getValue() != null) {
//            // TODO: NAE-1645 not needed anymore, fixed class of value
//            if (field.getValue().getValue() instanceof LocalDate)
//                value = DateUtils.localDateToZonedDate((LocalDate) field.getValue().getValue(), resource.getDateZoneId());
//            else if (field.getValue().getValue() instanceof Date)
//                value = ((Date) field.getValue().getValue()).toInstant().atZone(resource.getDateZoneId());
//            return DateTimeFormatter.ofPattern(resource.getDateFormat().getValue()).format(value);
//        } else {
//            return "";
//        }
//    }
//}
