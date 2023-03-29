package com.netgrif.application.engine.pdf.generator.service;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.LayoutType;
import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.*;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PdfDataHelper implements IPdfDataHelper {

    @Autowired
    private ITaskService taskService;
    @Autowired
    private IUserService userService;
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

    @Getter
    @Setter
    private List<String> excludedFields;

    @Getter
    @Setter
    private PdfResource resource;

    @Getter
    @Setter
    private Stack<PdfField> changedPdfFields;

    @Getter
    @Setter
    private int lastX, lastY;

    @Getter
    @Setter
    private int originalCols;

    @Override
    public void setupDataHelper(PdfResource resource) {
        log.info("Setting up data helper for PDF generator...");
        this.resource = resource;
        this.pdfFields = new ArrayList<>();
        this.dataGroups = new ArrayList<>();
        this.changedPdfFields = new Stack<>();
        this.excludedFields = new ArrayList<>();
    }

    @Override
    public void setTaskId(Case useCase, Transition transition) {
        if (transition.getLayout() != null && transition.getLayout().getCols() != null)
            resource.setFormGridCols(transition.getLayout().getCols());
        QTask qTask = new QTask("task");
        this.taskId = taskService.searchOne(qTask.transitionId.eq(transition.getStringId()).and(qTask.caseId.eq(useCase.getId().toString()))).getStringId();
        this.originalCols = resource.getFormGridCols();
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

        this.dataGroups = dataService.getDataGroups(taskId, resource.getTextLocale(), userService.getLoggedOrSystem()).getData();

        dataGroups.forEach(dataGroup -> {
            if (dataGroup.getParentTaskRefId() == null) {
                refreshGrid(dataGroup);
                generateFromDataGroup(dataGroup);
                this.lastX = Integer.MAX_VALUE;
            }
        });
        Collections.sort(pdfFields);
    }

    private void generateFromDataGroup(DataGroup dataGroup) {
        Collection<DataRef> dataRefs = dataGroup.getDataRefs().values();
        if (isGridLayout(dataGroup)) {
            dataRefs = dataRefs.stream().sorted(Comparator.<DataRef, Integer>comparing(f -> f.getLayout().getY()).thenComparing(f -> f.getLayout().getX())).collect(Collectors.toList());
        }
        dataRefs.forEach(dataRef -> {
            Field<?> field = dataRef.getField();
            if (field.getType().equals(DataType.TASK_REF)) {
                Optional<DataGroup> taskRefGroup = this.dataGroups.stream().filter(dg -> Objects.equals(dg.getParentTaskRefId(), field.getStringId())).findFirst();
                taskRefGroup.ifPresent(this::generateFromDataGroup);
            } else {
                generateField(dataGroup, dataRef);
            }
        });
    }

    private boolean isGridLayout(DataGroup dataGroup) {
        return dataGroup.getLayout() != null && dataGroup.getLayout().getType() != null && dataGroup.getLayout().getType().equals(LayoutType.GRID);
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

    protected void generateField(DataGroup dataGroup, DataRef dataRef) {
        Field<?> field = dataRef.getField();
        if (isNotHidden(field, dataGroup.getParentTransitionId()) && isNotExcluded(field.getStringId())) {
            PdfField pdfField = null;
//            TODO: release/7.0.0 fix, builder and registry for each type and component
            switch (field.getType()) {
                case BUTTON:
                case TASK_REF:
                    break;
                case ENUMERATION_MAP:
                    pdfField = createEnumMapField(dataGroup, dataRef);
                    pdfFields.add(pdfField);
                    break;
                case ENUMERATION:
                    pdfField = createEnumField(dataGroup, dataRef);
                    pdfFields.add(pdfField);
                    break;
                case MULTICHOICE_MAP:
                    pdfField = createMultiChoiceMapField(dataGroup, dataRef);
                    pdfFields.add(pdfField);
                    break;
                case MULTICHOICE:
                    pdfField = createMultiChoiceField(dataGroup, dataRef);
                    pdfFields.add(pdfField);
                    break;
                case I_18_N:
                    // TODO: release/7.0.0 dataRef component?
                    if (field.getComponent() != null && Objects.equals(field.getComponent().getName(), Component.DIVIDER)) {
                        pdfField = createI18nDividerField(dataGroup, dataRef);
                        pdfFields.add(pdfField);
                    }
                    break;
                default:
                    pdfField = createPdfTextField(dataGroup, dataRef);
                    pdfFields.add(pdfField);
                    break;
            }
            if (pdfField != null) {
                generatePdfDataGroup(dataGroup, pdfField);
            }
        }
    }

    protected PdfField createPdfTextField(DataGroup dataGroup, DataRef field) {
        TextFieldBuilder builder = new TextFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createEnumField(DataGroup dataGroup, DataRef field) {
        EnumerationFieldBuilder builder = new EnumerationFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createMultiChoiceField(DataGroup dataGroup, DataRef field) {
        MultiChoiceFieldBuilder builder = new MultiChoiceFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createEnumMapField(DataGroup dataGroup, DataRef field) {
        EnumerationMapFieldBuilder builder = new EnumerationMapFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createMultiChoiceMapField(DataGroup dataGroup, DataRef field) {
        MultiChoiceMapFieldBuilder builder = new MultiChoiceMapFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected PdfField createI18nDividerField(DataGroup dataGroup, DataRef field) {
        I18nDividerFieldBuilder builder = new I18nDividerFieldBuilder(resource);
        PdfField pdfField = builder.buildField(dataGroup, field, lastX, lastY);
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    protected void updateLastCoordinates(int lastX, int lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
    }

    protected int updateBottomY(PdfField pdfField) {
        return FieldBuilder.countBottomPosY(pdfField, pdfField.getResource());
    }

    protected void shiftFields(PdfField currentField) {
        pdfFields.forEach(field -> {
            if (currentField != field) {
                shiftField(currentField, field);
            }
        });
    }

    protected void shiftField(PdfField currentField, PdfField fieldBelow) {
        int belowTopY, cFieldBottomY;
        belowTopY = fieldBelow.getTopY();
        cFieldBottomY = currentField.getBottomY();
        if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY > belowTopY)) {
            shiftDown(belowTopY, cFieldBottomY, fieldBelow, currentField.getResource());
        }
    }

    protected void generatePdfDataGroup(DataGroup dataGroup, PdfField pdfField) {
        PdfField dgField;
        if (dataGroup != null && dataGroup.getTitle() != null) {
            dgField = new DataGroupFieldBuilder(resource).buildField(dataGroup, pdfField);
            if (!pdfFields.contains(dgField)) {
                pdfFields.add(dgField);
            }
        }
    }

    protected void shiftDown(int belowTopY, int cFieldBottomY, PdfField fieldBelow, PdfResource resource) {
        int currentDiff;
        currentDiff = cFieldBottomY - belowTopY + resource.getPadding();
        fieldBelow.setTopY(belowTopY + currentDiff);
        fieldBelow.setBottomY(fieldBelow.getBottomY() + currentDiff);
        fieldBelow.setChangedPosition(true);
        if (!changedPdfFields.contains(fieldBelow)) {
            changedPdfFields.push(fieldBelow);
        }
    }

    protected boolean isCoveredByDataGroup(PdfField currentField, PdfField fieldBelow) {
        return currentField.isDgField() && currentField.getOriginalTopY() <= fieldBelow.getOriginalTopY();
    }

    protected boolean isCoveredByDataField(PdfField currentField, PdfField fieldBelow) {
        return currentField.getOriginalBottomY() < fieldBelow.getOriginalTopY();
    }

    protected void refreshGrid(DataGroup dataGroup) {
        log.info("Refreshing grid for data group in PDF...");
        int cols = this.originalCols;
        if (dataGroup.getLayout() != null && dataGroup.getLayout().getCols() != null) {
            cols = dataGroup.getLayout().getCols();
        }
        resource.setFormGridCols(cols);
        resource.updateProperties();
    }

    protected boolean isNotHidden(Field<?> field, String transitionId) {
        DataFieldBehavior fieldBehavior = field.getBehaviors().get(transitionId);
        if (fieldBehavior == null) {
            return true;
        }
        return fieldBehavior.getBehavior() != FieldBehavior.HIDDEN;
    }

    protected boolean isNotExcluded(String fieldId) {
        return !excludedFields.contains(fieldId);
    }
}
