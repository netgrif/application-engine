package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfMultiChoiceField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedMultichoiceField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiChoiceFieldBuilder extends SelectionFieldBuilder {

    public MultiChoiceFieldBuilder(PdfResource resource) {
        super(resource);
    }

    public PdfField buildField(DataGroup dataGroup, LocalisedMultichoiceField field, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
        List<String> choices;
        List<String> values = new ArrayList<>();
        choices = field.getChoices();
        if (dataSet.get(field.getStringId()).getValue() != null) {
            for (I18nString value : (List<I18nString>) dataSet.get(field.getStringId()).getValue()) {
                values.add(value.getTranslation(resource.getTextLocale()));
            }
        }
        String translatedTitle = getTranslatedLabel(field.getStringId(), petriNet);
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
