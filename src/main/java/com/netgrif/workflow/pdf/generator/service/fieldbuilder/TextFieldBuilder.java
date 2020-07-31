package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfTextField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

public class TextFieldBuilder extends FieldBuilder{

    public TextFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedField field, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
        String value;
        switch (field.getType()) {
            case DATE:
                value = formatDate(field, dataSet);
                break;
            case DATETIME:
                value = formatDateTime(field, dataSet);
                break;
            case NUMBER:
                double number = (double) dataSet.get(field.getStringId()).getValue();
                NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
                value = nf2.format(number);
                break;
            default:
                value = dataSet.get(field.getStringId()).getValue() != null ? dataSet.get(field.getStringId()).getValue().toString() : "";
                break;
        }
        String translatedTitle = getTranslatedLabel(field.getStringId(), petriNet);
        PdfField pdfField = new PdfTextField(field.getStringId(), dataGroup, field.getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    private String formatDate(LocalisedField field, Map<String, DataField> dataSet) {
        if (dataSet.get(field.getStringId()).getValue() != null) {
            return new SimpleDateFormat(resource.getDateFormat().getValue()).format(dataSet.get(field.getStringId()).getValue());
        } else {
            return "";
        }
    }

    private String formatDateTime(LocalisedField field, Map<String, DataField> dataSet) {
        if (dataSet.get(field.getStringId()).getValue() != null) {
            return new SimpleDateFormat(resource.getDateTimeFormat().getValue()).format(dataSet.get(field.getStringId()).getValue());
        } else {
            return "";
        }
    }
}
