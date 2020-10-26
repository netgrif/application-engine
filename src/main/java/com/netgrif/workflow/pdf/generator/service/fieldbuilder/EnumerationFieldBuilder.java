package com.netgrif.workflow.pdf.generator.service.fieldbuilder;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfEnumerationField;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfSelectionField;
import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.workflow.domain.DataField;

import java.util.*;
import java.util.stream.Collectors;

public class EnumerationFieldBuilder extends SelectionFieldBuilder {

    public EnumerationFieldBuilder(PdfResource resource) {
        super(resource);
    }

    @Override
    protected List<String> getTranslatedSet(Set<I18nString> choices) {
        return choices.stream().map(s -> s.getTranslation(resource.getTextLocale())).collect(Collectors.toList());
    }

    @Override
    protected String getTranslatedString(Set<I18nString> choices, String value) {
        return choices.stream().filter(s -> s.toString().equals(value) || (s.getKey() != null && s.getKey().equals(value)))
                .map(s -> s.getTranslation(resource.getTextLocale())).findAny().get();
    }

    public PdfField buildField(DataGroup dataGroup, String fieldId, DataFieldLogic fieldLogic, Map<String, DataField> dataSet, PetriNet petriNet,
                               int lastX, int lastY){
        List<String> choices = new ArrayList<>();;
        List<String> values = new ArrayList<>();
        this.lastX = lastX;
        this.lastY = lastY;
        FieldType type = petriNet.getDataSet().get(fieldId).getType();

        switch (type) {
            case ENUMERATION_MAP:
                choices = getTranslatedSet(resolveOptions(((EnumerationMapField)petriNet.getDataSet().get(fieldId)).getOptions()));
                if (dataSet.get(fieldId).getValue() != null) {
                    values.add(getTranslatedString(resolveOptions(((EnumerationMapField)petriNet.getDataSet().get(fieldId)).getOptions()), dataSet.get(fieldId).getValue().toString()));
                }
                break;
            case ENUMERATION:
                choices = getTranslatedSet(((EnumerationField)petriNet.getDataSet().get(fieldId)).getChoices());
                if (dataSet.get(fieldId).getValue() != null) {
                    values.add(getTranslatedString(((EnumerationField)petriNet.getDataSet().get(fieldId)).getChoices(), dataSet.get(fieldId).getValue().toString()));
                }
                break;
            default:
                break;
        }

        String translatedTitle = getTranslatedLabel(fieldId, petriNet);
        PdfSelectionField pdfField = new PdfEnumerationField(fieldId, dataGroup, type, translatedTitle, values, choices, resource);
        setFieldParams(dataGroup, fieldLogic, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }
}
