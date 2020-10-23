package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfMultiChoiceField;
import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import com.netgrif.workflow.workflow.domain.DataField;

import java.util.*;
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


    public PdfField buildField(DataGroup dataGroup, String fieldId, DataFieldLogic fieldLogic, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        List<String> choices = new ArrayList<>();;
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;
        FieldType type = petriNet.getDataSet().get(fieldId).getType();

        switch (type) {
            case MULTICHOICE_MAP:
                choices = getTranslatedSet(new HashSet<>(((MultichoiceMapField)petriNet.getDataSet().get(fieldId)).getOptions().values()));
                if (dataSet.get(fieldId).getValue() != null) {
                    for (I18nString value : (List<I18nString>) dataSet.get(fieldId).getValue()) {
                        values.add(getTranslatedString(new HashSet<>(((MultichoiceMapField)petriNet.getDataSet().get(fieldId)).getOptions().values()), value.toString()));
                    }
                }
                break;
            case MULTICHOICE:
                choices = getTranslatedSet(((MultichoiceField)petriNet.getDataSet().get(fieldId)).getChoices());
                if (dataSet.get(fieldId).getValue() != null) {
                    values = getTranslatedSet(((MultichoiceField)petriNet.getDataSet().get(fieldId)).getValue());
                    for (I18nString value : (List<I18nString>) dataSet.get(fieldId).getValue()) {
                        values.add(getTranslatedString(((MultichoiceField)petriNet.getDataSet().get(fieldId)).getChoices(), value.toString()));
                    }
                }
                break;
            default:
                break;
        }

        String translatedTitle = getTranslatedLabel(fieldId, petriNet);
        PdfMultiChoiceField pdfField = new PdfMultiChoiceField(fieldId, dataGroup, petriNet.getDataSet().get(fieldId).getType(), translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, fieldLogic, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
