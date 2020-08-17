package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfMultiChoiceField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceField;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedMultichoiceField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiChoiceFieldBuilder extends SelectionFieldBuilder {

    public MultiChoiceFieldBuilder(PdfResource resource) {
        super(resource);
    }

    @Override
    protected List<String> getTranslatedSet(Set<I18nString> choices) {
        return choices.stream().map(s -> s.getTranslation(resource.getTextLocale())).collect(Collectors.toList());
    }

    @Override
    protected String getTranslatedString(Set<I18nString> choices, String value) {
        return choices.stream().filter(s -> s.toString().equals(value)).map(s -> s.getTranslation(resource.getTextLocale())).findAny().get();
    }


    public PdfField buildField(DataGroup dataGroup, LocalisedMultichoiceField field, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
        List<String> choices;
        List<String> values = new ArrayList<>();
        choices = getTranslatedSet(((MultichoiceField)petriNet.getDataSet().get(field.getStringId())).getChoices());
        if (dataSet.get(field.getStringId()).getValue() != null) {
            values = getTranslatedSet(((MultichoiceField)petriNet.getDataSet().get(field.getStringId())).getValue());
            for (I18nString value : (List<I18nString>) dataSet.get(field.getStringId()).getValue()) {
                values.add(getTranslatedString(((MultichoiceField)petriNet.getDataSet().get(field.getStringId())).getChoices(), value.toString()));
            }
        }
        String translatedTitle = getTranslatedLabel(field.getStringId(), petriNet);
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(field.getStringId(), dataGroup, field.getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
