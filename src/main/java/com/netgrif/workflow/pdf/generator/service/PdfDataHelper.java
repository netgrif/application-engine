package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.fieldbuilder.*;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.workflow.domain.DataField;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class PdfDataHelper implements IPdfDataHelper {

    private PdfResource resource;

    @Getter
    @Setter
    private PetriNet petriNet;

    @Getter
    @Setter
    private Transition transition;

    @Getter
    @Setter
    private Map<String, DataGroup> dataGroups;

    @Getter
    @Setter
    private Map<String, DataFieldLogic> fieldLogicMap;

    @Getter
    @Setter
    private Map<String, DataField> dataSet;

    @Getter
    @Setter
    private List<PdfField> pdfFields;

    private Stack<PdfField> changedPdfFields;

    private int lastX, lastY, lastLayoutY;

    @Override
    public void setupDataHelper(PdfResource resource){
        this.resource = resource;
        this.pdfFields = new ArrayList<>();
        this.changedPdfFields = new Stack<>();
    }

    @Override
    public void generateTitleField() {
        log.info("Setting title field for PDF");
        resource.setBaseY(resource.getPageHeight() - resource.getMarginTitle());
        PdfField titleField = new TitleFieldBuilder(resource).createTitleField();
        pdfFields.add(titleField);
        resource.setBaseY(resource.getBaseY() - titleField.getHeight());
    }

    @Override
    public void generatePdfFields() {
        log.info("Generating PDF fields from data fields.");

        lastX = Integer.MAX_VALUE;
        lastY = 0;
        dataGroups.forEach((dataGroupId, dataGroup) ->{
                refreshGrid(dataGroup);
                dataGroup.getData().forEach(field -> {
                            generateField(dataGroup, field, fieldLogicMap.get(field));
                        }
                );});
        Collections.sort(pdfFields);
    }

    @Override
    public void generatePdfDataGroups() {
        log.info("Generating PDF field from data group titles.");

        List<PdfField> dgFields = new ArrayList<>();
        DataGroup currentDg = null;
        for (PdfField pdfField : pdfFields) {
            if (pdfField.getDataGroup() != null && pdfField.getDataGroup().getTitle() != null && pdfField.getDataGroup() != currentDg) {
                currentDg = pdfField.getDataGroup();
                PdfField dgField = new DataGroupFieldBuilder(resource).buildField(pdfField.getDataGroup(), pdfField);
                dgFields.add(dgField);
            }
        }
        pdfFields.addAll(dgFields);
        Collections.sort(pdfFields);
    }

    @Override
    public void correctFieldsPosition() {
        log.info("Correcting field positions for correct export to PDF.");
        pdfFields.forEach(pdfField -> {
            if (pdfField.isChangedSize()) {
                pdfField.setBottomY(updateBottomY(pdfField));
                changedPdfFields.push(pdfField);
            }
        });

        while (!changedPdfFields.empty()) {
            PdfField pdfField = changedPdfFields.pop();
            if (pdfField.isChangedSize()) {
                shiftFieldsBelow(pdfField);
            }
            if (pdfField.isChangedPosition()) {
                shiftFieldsBelow(pdfField);
            }
        }
    }

    protected void generateField(DataGroup dataGroup, String fieldId, DataFieldLogic fieldLogic) {
        if (isNotHidden(fieldId)) {
            switch (petriNet.getDataSet().get(fieldId).getType()) {
                case BUTTON:
                case FILE:
                    break;
                case ENUMERATION:
                    pdfFields.add(createEnumField(dataGroup, fieldId, fieldLogic));
                    break;
                case MULTICHOICE:
                    pdfFields.add(createMultiChoiceField(dataGroup, fieldId, fieldLogic));
                    break;
                default:
                    pdfFields.add(createPdfTextField(dataGroup, fieldId, fieldLogic));
                    break;
            }
        }
    }

    protected PdfField createPdfTextField(DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        TextFieldBuilder builder = new TextFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createEnumField(DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        EnumerationFieldBuilder builder = new EnumerationFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createMultiChoiceField(DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        MultiChoiceFieldBuilder builder = new MultiChoiceFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected void updateLastCoordinates(int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
    }

    protected int updateBottomY(PdfField pdfField){
        return FieldBuilder.countBottomPosY(pdfField, pdfField.getResource());
    }

    private void shiftFieldsBelow(PdfField currentField) {
        pdfFields.forEach(fieldBelow -> {
            if (currentField != fieldBelow) {
                shiftField(currentField, fieldBelow);
            }
        });
    }

    protected void shiftField(PdfField currentField, PdfField fieldBelow){
        int belowTopY, cFieldBottomY;
        belowTopY = fieldBelow.getTopY();
        cFieldBottomY = currentField.getBottomY();
        if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY > belowTopY)) {
            shiftDown(belowTopY, cFieldBottomY, fieldBelow, currentField.getResource());
        } else if(isEmptySpace(currentField, fieldBelow) && isNearestToBelow(currentField, fieldBelow)){
            shiftUp(belowTopY, cFieldBottomY, fieldBelow, currentField.getResource());
        }
    }

    private void shiftDown(int belowTopY, int cFieldBottomY, PdfField fieldBelow, PdfResource resource) {
        int currentDiff;
        currentDiff = cFieldBottomY - belowTopY + resource.getPadding();
        fieldBelow.setTopY(belowTopY + currentDiff);
        fieldBelow.setBottomY(fieldBelow.getBottomY() + currentDiff);
        fieldBelow.setChangedPosition(true);
        if (!changedPdfFields.contains(fieldBelow)) {
            changedPdfFields.push(fieldBelow);
        }
    }

    private void shiftUp(int belowTopY, int cFieldBottomY, PdfField fieldBelow, PdfResource resource){
        int currentDiff;
        currentDiff = belowTopY - cFieldBottomY - resource.getPadding();
        fieldBelow.setTopY(belowTopY - currentDiff);
        fieldBelow.setBottomY(fieldBelow.getBottomY() - currentDiff);
        fieldBelow.setChangedPosition(true);
        if (!changedPdfFields.contains(fieldBelow)) {
            changedPdfFields.push(fieldBelow);
        }
    }

    private boolean isCoveredByDataGroup(PdfField currentField, PdfField fieldBelow) {
        return currentField.isDgField() && currentField.getOriginalTopY() <= fieldBelow.getOriginalTopY();
    }

    private boolean isCoveredByDataField(PdfField currentField, PdfField fieldBelow) {
        return currentField.getOriginalBottomY() < fieldBelow.getOriginalTopY();
    }

    private boolean isNearestToBelow(PdfField currentField, PdfField fieldBelow){
        int difference = fieldBelow.getTopY() - currentField.getBottomY();

        if(currentField.isDgField() && fieldBelow.getOriginalTopY() == fieldBelow.getOriginalTopY()){
            return true;
        }

        for(PdfField field : pdfFields) {
            int tempDiff = fieldBelow.getTopY() - field.getBottomY();
            if(fieldBelow.getOriginalTopY() != field.getOriginalTopY() && difference > tempDiff && tempDiff > 0){
                return false;
            }
        }
        return true;
    }

    private boolean isEmptySpace(PdfField currentField, PdfField fieldBelow){
        return (fieldBelow.getTopY() - currentField.getBottomY()) > 2 * resource.getLineHeight();
    }

    protected void refreshGrid(DataGroup dataGroup){
        if(dataGroup.getLayout() != null){
            Integer cols = dataGroup.getLayout().getCols();
            resource.setFormGridCols(cols == null ? resource.getFormGridCols() : cols);
            resource.updateProperties();
        }
    }

    private boolean isNotHidden(String fieldId){
        boolean result = true;
        if(transition.getDataSet().get(fieldId).getBehavior().contains(FieldBehavior.HIDDEN)){
            result = false;
        }
        if(dataSet.get(fieldId).getBehavior().size() > 0
                && (dataSet.get(fieldId).getBehavior().get(transition.getStringId()).contains(FieldBehavior.EDITABLE)
                || dataSet.get(fieldId).getBehavior().get(transition.getStringId()).contains(FieldBehavior.VISIBLE))){
            result = true;
        }
        return result;
    }
}
