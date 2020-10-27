package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfTaskRefField;
import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.DataField;

import java.util.List;
import java.util.Map;

public class TaskRefFieldBuilder extends FieldBuilder{

    public TaskRefFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfTaskRefField buildField(DataGroup dataGroup, String fieldId, DataFieldLogic fieldLogic, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
        List<String> value = (List<String>) dataSet.get(fieldId).getValue();

        String translatedTitle = getTranslatedLabel(fieldId, petriNet);
        PdfTaskRefField pdfField = new PdfTaskRefField(fieldId, dataGroup, petriNet.getDataSet().get(fieldId).getType(), translatedTitle, value, resource);
        setFieldParams(dataGroup, fieldLogic, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
