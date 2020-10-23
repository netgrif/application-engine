package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfTaskRefField;
import com.netgrif.workflow.pdf.generator.domain.PdfTextField;
import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.workflow.domain.DataField;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class TextFieldBuilder extends FieldBuilder{

    public TextFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, String fieldId, DataFieldLogic fieldLogic, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
        String value;
        switch (petriNet.getDataSet().get(fieldId).getType()) {
            case DATE:
                value = formatDate(fieldId, dataSet);
                break;
            case DATETIME:
                value = formatDateTime(fieldId, dataSet);
                break;
            case NUMBER:
                double number = (double) dataSet.get(fieldId).getValue();
                NumberFormat nf2 = NumberFormat.getInstance(resource.getNumberFormat());
                value = nf2.format(number);
                break;
            case FILE:
                value = dataSet.get(fieldId).getValue() != null ? ((FileFieldValue)dataSet.get(fieldId).getValue()).getName() : "";
                break;
            default:
                value = dataSet.get(fieldId).getValue() != null ? dataSet.get(fieldId).getValue().toString() : "";
                break;
        }
        String translatedTitle = getTranslatedLabel(fieldId, petriNet);
        PdfField pdfField = new PdfTextField(fieldId, dataGroup, petriNet.getDataSet().get(fieldId).getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, fieldLogic, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    private String formatDate(String fieldId, Map<String, DataField> dataSet) {
        if (dataSet.get(fieldId).getValue() != null) {
            return new SimpleDateFormat(resource.getDateFormat().getValue()).format(dataSet.get(fieldId).getValue());
        } else {
            return "";
        }
    }

    private String formatDateTime(String fieldId, Map<String, DataField> dataSet) {
        if (dataSet.get(fieldId).getValue() != null) {
            return new SimpleDateFormat(resource.getDateTimeFormat().getValue()).format(dataSet.get(fieldId).getValue());
        } else {
            return "";
        }
    }
}
