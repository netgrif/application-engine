package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.domain.PdfTaskRefField;
import com.netgrif.workflow.pdf.generator.service.fieldbuilder.*;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.workflow.petrinet.domain.*;
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class PdfDataHelper implements IPdfDataHelper {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IWorkflowService workflowService;

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

    private List<PdfTaskRefField> taskRefFields;

    private Stack<PdfField> changedPdfFields;

    private int lastX, lastY;

    @Override
    public void setupDataHelper(PdfResource resource){
        this.resource = resource;
        this.pdfFields = new ArrayList<>();
        this.taskRefFields = new ArrayList<>();
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
        generate(dataGroups, petriNet, transition, dataSet, fieldLogicMap);
        resolveTaskRefOffset();
        Collections.sort(pdfFields);
    }

    private void generatePdfDataGroup(DataGroup dataGroup, PdfField pdfField) {
        log.info("Generating PDF field from data group titles.");
        PdfField dgField = null;
        if (dataGroup != null && dataGroup.getTitle() != null) {
            dgField = new DataGroupFieldBuilder(resource).buildField(dataGroup, pdfField);
        }
        if (dgField != null && !pdfFields.contains(dgField)) {
            pdfFields.add(dgField);
        }
    }

    private void resolveTaskRefOffset(){
        pdfFields.sort(Comparator.comparingInt(PdfField::getLayoutY));
        Collections.sort(taskRefFields);
        for(PdfField field : pdfFields) {
            for (PdfTaskRefField taskRefField : taskRefFields) {
                if (!taskRefField.getPdfFieldIds().contains(field.getFieldId()) && taskRefField.getLayoutY() < field.getLayoutY() && (taskRefField.getLayoutY() + taskRefField.getRows()) >= field.getLayoutY()) {
                    field.setLayoutY(taskRefField.getLayoutY() + taskRefField.getRows() + 1);
                    field.setOriginalTopY(updateTopY(field));
                    field.setOriginalBottomY(updateBottomY(field));
                    field.setTopY(field.getOriginalTopY());
                    field.setBottomY(field.getOriginalBottomY());
                    field.setChangedPosition(true);
                    taskRefField.setRows(taskRefField.getRows() + 1);
                    break;
                }
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
            if (pdfField.isChangedPosition()) {
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

    protected void generate(Map<String, DataGroup> dataGroups, PetriNet petriNet, Transition transition, Map<String, DataField> dataSet, Map<String, DataFieldLogic> fieldLogicMap){
        dataGroups.forEach((dataGroupId, dataGroup) ->{
            refreshGrid(dataGroup);
            dataGroup.getData().forEach(field -> {
                        generateField(petriNet, transition, dataSet, dataGroup, field, fieldLogicMap.get(field));
                    }
            );});
    }

    protected void generateField(PetriNet petriNet, Transition transition, Map<String, DataField> dataSet, DataGroup dataGroup, String fieldId, DataFieldLogic fieldLogic) {
        if (isNotHidden(fieldId, transition, dataSet)) {
            PdfField pdfField = null;
            switch (petriNet.getDataSet().get(fieldId).getType()) {
                case BUTTON:
                case FILE:
                    break;
                case TASK_REF:
                    pdfField = createTaskRefField(petriNet, dataSet, dataGroup, fieldId, fieldLogic);
                    if (pdfField != null)
                        taskRefFields.add((PdfTaskRefField) pdfField);
                    break;
                case ENUMERATION:
                    pdfField = createEnumField(petriNet, dataSet, dataGroup, fieldId, fieldLogic);
                    pdfFields.add(pdfField);
                    break;
                case MULTICHOICE:
                    pdfField = createMultiChoiceField(petriNet, dataSet, dataGroup, fieldId, fieldLogic);
                    pdfFields.add(pdfField);
                    break;
                default:
                    pdfField = createPdfTextField(petriNet, dataSet, dataGroup, fieldId, fieldLogic);
                    pdfFields.add(pdfField);
                    break;
            }
            if(pdfField != null)
                generatePdfDataGroup(dataGroup, pdfField);
        }
    }

    protected PdfTaskRefField createTaskRefField(PetriNet petriNet, Map<String, DataField> dataSet, DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        TaskRefFieldBuilder builder = new TaskRefFieldBuilder(resource);
        PdfTaskRefField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return createTaskRefFields(dataSet, fieldId, pdfField);
    }

    protected PdfField createPdfTextField(PetriNet petriNet, Map<String, DataField> dataSet, DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        TextFieldBuilder builder = new TextFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createEnumField(PetriNet petriNet, Map<String, DataField> dataSet, DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        EnumerationFieldBuilder builder = new EnumerationFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createMultiChoiceField(PetriNet petriNet, Map<String, DataField> dataSet, DataGroup dataGroup, String fieldId, DataFieldLogic field) {
        MultiChoiceFieldBuilder builder = new MultiChoiceFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, fieldId, field, dataSet, petriNet, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfTaskRefField createTaskRefFields(Map<String, DataField> dataSet, String fieldId, PdfTaskRefField pdfField){
        List<String> taskRefValues = (List<String>) dataSet.get(fieldId).getValue();
        if(taskRefValues == null) {
            return  null;
        }
        List<Task> tasks = taskService.findAllById(taskRefValues);
        tasks.forEach(task -> createTaskRefField(task, pdfField));
        return pdfField;
    }

    protected void createTaskRefField(Task task, PdfTaskRefField pdfField){
        Case taskRefCase = workflowService.findOne(task.getCaseId());
        Map<String, DataFieldLogic> taskRefDataSet = taskRefCase.getPetriNet().getTransition(task.getTransitionId()).getDataSet();
        updateTaskRefFieldLayout(taskRefDataSet, pdfField);
        Map<String, DataGroup> dataGroups = taskRefCase.getPetriNet().getTransition(task.getTransitionId()).getDataGroups();
        resolveTaskRefFields(taskRefCase.getPetriNet().getTransition(task.getTransitionId()), pdfField, taskRefCase.getDataSet(), taskRefDataSet, dataGroups);
        generate(dataGroups, taskRefCase.getPetriNet(), taskRefCase.getPetriNet().getTransition(task.getTransitionId()), taskRefCase.getDataSet(), taskRefDataSet);
    }

    protected void updateTaskRefFieldLayout(Map<String, DataFieldLogic> taskRefDataSet, PdfTaskRefField pdfField){
        int y = lastY;
        for (Map.Entry<String, DataFieldLogic> field : taskRefDataSet.entrySet()) {
            if (field.getValue().getLayout().getX() == 0) {
                field.getValue().getLayout().setY(++y);
                pdfField.setRows(pdfField.getRows() + 1);
            } else {
                field.getValue().getLayout().setY(y);
            }

        }
    }

    protected void resolveTaskRefFields(Transition transition, PdfTaskRefField pdfField, Map<String, DataField> dataSet, Map<String, DataFieldLogic> taskRefDataSet, Map<String, DataGroup> dataGroups){
        dataGroups.forEach((id, dataGroup) ->{
            if(dataGroup.getTitle() != null){
                pdfField.getPdfFieldIds().add(id);
            }
        });
        taskRefDataSet.forEach((field, logic) ->{
            if(isNotHidden(field, transition, dataSet)){
                pdfField.getPdfFieldIds().add(field);
            }
        });
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
                shiftField(field, currentField);
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
        if (isEmptySpace(currentField, fieldBelow) && isNearestToBelow(currentField, fieldBelow)){
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

        if(currentField.isDgField() && currentField.getOriginalTopY() == fieldBelow.getOriginalTopY()){
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

    private boolean isNotHidden(String fieldId, Transition transition, Map<String, DataField> dataSet){
        boolean result = true;
        if(transition.getDataSet().get(fieldId) == null || transition.getDataSet().get(fieldId).getBehavior().contains(FieldBehavior.HIDDEN)){
            result = false;
        }
        if(dataSet.get(fieldId) != null && dataSet.get(fieldId).getBehavior().size() > 0
                && (dataSet.get(fieldId).getBehavior().get(transition.getStringId()).contains(FieldBehavior.EDITABLE)
                || dataSet.get(fieldId).getBehavior().get(transition.getStringId()).contains(FieldBehavior.VISIBLE))){
            result = true;
        }
        return result;
    }
}
