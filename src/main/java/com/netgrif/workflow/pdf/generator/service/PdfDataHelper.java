package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.fieldbuilder.*;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QTask;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

@Slf4j
@Service
public class PdfDataHelper implements IPdfDataHelper {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IDataService dataService;

    @Getter
    @Setter
    private PetriNet petriNet;

    @Getter
    @Setter
    private String taskId;

    @Getter
    @Setter
    private List<DataGroup> dataGroups;

    @Getter
    @Setter
    private List<PdfField> pdfFields;

    private PdfResource resource;

    private Stack<PdfField> changedPdfFields;

    private int lastX, lastY;

    @Override
    public void setupDataHelper(PdfResource resource){
        this.resource = resource;
        this.pdfFields = new ArrayList<>();
        this.dataGroups = new ArrayList<>();
        this.changedPdfFields = new Stack<>();
    }

    @Override
    public void setTaskId(Case useCase, Transition transition) {
        QTask qTask = new QTask("task");
        this.taskId = taskService.searchOne(qTask.transitionId.eq(transition.getStringId()).and(qTask.caseId.eq(useCase.get_id().toString()))).getStringId();
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

        this.dataGroups = dataService.getDataGroups(taskId, resource.getTextLocale());

        dataGroups.forEach(dataGroup ->{
            refreshGrid(dataGroup);
            dataGroup.getFields().getContent().forEach(field -> {
                        generateField(dataGroup, field);
                    }
            );
            this.lastX = Integer.MAX_VALUE;
        });
        Collections.sort(pdfFields);
    }

    private void generatePdfDataGroup(DataGroup dataGroup, PdfField pdfField) {
        log.info("Generating PDF field from data group titles.");
        PdfField dgField = null;
        if (dataGroup != null && dataGroup.getTitle() != null) {
            dgField = new DataGroupFieldBuilder(resource).buildField(dataGroup, pdfField);
            if (!pdfFields.contains(dgField)) {
                pdfFields.add(dgField);
            }
        }
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
                shiftFields(pdfField);
            }
            if (pdfField.isChangedPosition()) {
                shiftFields(pdfField);
            }
        }
    }

    protected void generateField(DataGroup dataGroup, LocalisedField field) {
        if (isNotHidden(field)) {
            PdfField pdfField = null;
            switch (field.getType()) {
                case BUTTON:
                case TASK_REF:
                    break;
                case ENUMERATION_MAP:
                    pdfField = createEnumMapField(dataGroup, (LocalisedEnumerationMapField) field);
                    pdfFields.add(pdfField);
                    break;
                case ENUMERATION:
                    pdfField = createEnumField(dataGroup, (LocalisedEnumerationField) field);
                    pdfFields.add(pdfField);
                    break;
                case MULTICHOICE_MAP:
                    pdfField = createMultiChoiceMapField(dataGroup, (LocalisedMultichoiceMapField) field);
                    pdfFields.add(pdfField);
                    break;
                case MULTICHOICE:
                    pdfField = createMultiChoiceField(dataGroup, (LocalisedMultichoiceField) field);
                    pdfFields.add(pdfField);
                    break;
                default:
                    pdfField = createPdfTextField(dataGroup, field);
                    pdfFields.add(pdfField);
                    break;
            }
            if(pdfField != null)
                generatePdfDataGroup(dataGroup, pdfField);
        }
    }

    protected PdfField createPdfTextField(DataGroup dataGroup, LocalisedField field) {
        TextFieldBuilder builder = new TextFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createEnumField(DataGroup dataGroup, LocalisedEnumerationField field) {
        EnumerationFieldBuilder builder = new EnumerationFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createMultiChoiceField(DataGroup dataGroup, LocalisedMultichoiceField field) {
        MultiChoiceFieldBuilder builder = new MultiChoiceFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createEnumMapField(DataGroup dataGroup, LocalisedEnumerationMapField field) {
        EnumerationMapFieldBuilder builder = new EnumerationMapFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createMultiChoiceMapField(DataGroup dataGroup, LocalisedMultichoiceMapField field) {
        MultiChoiceMapFieldBuilder builder = new MultiChoiceMapFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected void updateLastCoordinates(int lastX, int lastY){
        this.lastX = lastX;
        this.lastY = lastY;
    }

    protected int updateTopY(PdfField pdfField){
        return FieldBuilder.countTopPosY(pdfField, pdfField.getResource());
    }

    protected int updateBottomY(PdfField pdfField){
        return FieldBuilder.countBottomPosY(pdfField, pdfField.getResource());
    }

    private void shiftFields(PdfField currentField) {
        pdfFields.forEach(field -> {
            if (currentField != field) {
                shiftField(currentField, field);
//                shiftField(field, currentField);
            }
        });
    }

    protected void shiftField(PdfField currentField, PdfField fieldBelow){
        int belowTopY, cFieldBottomY;
        belowTopY = fieldBelow.getTopY();
        cFieldBottomY = currentField.getBottomY();
        if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY > belowTopY)) {
            shiftDown(belowTopY, cFieldBottomY, fieldBelow, currentField.getResource());
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

    private boolean isCoveredByDataGroup(PdfField currentField, PdfField fieldBelow) {
        return currentField.isDgField() && currentField.getOriginalTopY() <= fieldBelow.getOriginalTopY();
    }

    private boolean isCoveredByDataField(PdfField currentField, PdfField fieldBelow) {
        return currentField.getOriginalBottomY() < fieldBelow.getOriginalTopY();
    }

    private void refreshGrid(DataGroup dataGroup){
        if(dataGroup.getLayout() != null){
            Integer cols = dataGroup.getLayout().getCols();
            resource.setFormGridCols(cols == null ? resource.getFormGridCols() : cols);
            resource.updateProperties();
        }
    }

    private boolean isNotHidden(LocalisedField field){
        if(!field.getBehavior().has("hidden") || !field.getBehavior().get("hidden").asBoolean()){
            return true;
        }
        return false;
    }
}
