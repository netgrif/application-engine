package com.netgrif.application.engine.pdf.generator.service;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilder.*;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.responsebodies.*;
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

    private static final String DIVIDER = "divider";

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
        this.taskId = taskService.searchOne(qTask.transitionId.eq(transition.getStringId()).and(qTask.caseId.eq(useCase.get_id().toString()))).getStringId();
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

        this.dataGroups = dataService.getDataGroups(taskId, resource.getTextLocale()).getData();

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
        Collection<LocalisedField> fields = dataGroup.getFields().getContent();
        if (dataGroup.getLayout() != null && dataGroup.getLayout().getType() != null && dataGroup.getLayout().getType().equals("grid")) {
            fields = fields.stream().sorted(Comparator.<LocalisedField, Integer>comparing(f -> f.getLayout().getY()).thenComparing(f -> f.getLayout().getX())).collect(Collectors.toList());
        }
       fields.forEach(field -> {
                    if (field.getType().equals(FieldType.TASK_REF)) {
                        Optional<DataGroup> taskRefGroup = this.dataGroups.stream().filter(dg -> Objects.equals(dg.getParentTaskRefId(), field.getStringId())).findFirst();
                        taskRefGroup.ifPresent(this::generateFromDataGroup);
                    } else {
                        generateField(dataGroup, field);
                    }
                }
        );
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
        if (isNotHidden(field) && isNotExcluded(field.getStringId())) {
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
                case I18N:
                    if (field.getComponent() != null && Objects.equals(field.getComponent().getName(), DIVIDER)) {
                        pdfField = createI18nDividerField(dataGroup, (LocalisedI18nStringField) field);
                        pdfFields.add(pdfField);
                    }
                    break;
                default:
                    pdfField = createPdfTextField(dataGroup, field);
                    pdfFields.add(pdfField);
                    break;
            }
            if (pdfField != null)
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

    protected PdfField createI18nDividerField(DataGroup dataGroup, LocalisedI18nStringField field) {
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
        if (dataGroup.getLayout() != null && dataGroup.getLayout().getCols() != null) {
            Integer cols = dataGroup.getLayout().getCols();
            resource.setFormGridCols(cols == null ? this.originalCols : cols);
        } else {
            resource.setFormGridCols(this.originalCols);
        }
        resource.updateProperties();
    }

    protected boolean isNotHidden(LocalisedField field) {
        return !field.getBehavior().has("hidden") || !field.getBehavior().get("hidden").asBoolean();
    }

    protected boolean isNotExcluded(String fieldId) {
        return !excludedFields.contains(fieldId);
    }
}
