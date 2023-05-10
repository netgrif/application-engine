package com.netgrif.application.engine.pdf.generator.service;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.LayoutType;
import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.fields.*;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.*;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.blocks.*;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.application.engine.pdf.generator.utils.PdfGeneratorUtils;
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
import java.util.function.Function;
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
    private PdfDocumentContent pdfDocumentContent;

    @Getter
    @Setter
    private List<String> excludedFields;

    @Getter
    @Setter
    private PdfResource resource;

    @Getter
    @Setter
    private Stack<PdfField<?>> changedPdfFields;

    @Getter
    @Setter
    private int lastX, lastY;

    @Getter
    @Setter
    private Map<String, PdfFieldBuilder<?>> pdfFieldBuilders;

    public PdfDataHelper(List<PdfFieldBuilder<?>> builders) {
        this.pdfFieldBuilders = builders.stream().flatMap(b -> Arrays.stream(b.getType()).map(k -> new AbstractMap.SimpleEntry<>(k, b))).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    @Override
    public void setupDataHelper(PdfResource resource) {
        log.info("Setting up data helper for PDF generator...");
        this.resource = resource;
        this.pdfDocumentContent = new PdfDocumentContent();
        this.dataGroups = new ArrayList<>();
        this.changedPdfFields = new Stack<>();
        this.excludedFields = new ArrayList<>();
        pdfFieldBuilders.values().parallelStream().forEach(b -> b.setResource(resource));
    }

    @Override
    public void setTaskId(Case useCase, Transition transition) {
        if (transition.getLayout() != null && transition.getLayout().getCols() != null)
            resource.setFormGridCols(transition.getLayout().getCols());
        QTask qTask = new QTask("task");
        this.taskId = taskService.searchOne(qTask.transitionId.eq(transition.getStringId()).and(qTask.caseId.eq(useCase.getId().toString()))).getStringId();
        resource.setOriginalCols(resource.getFormGridCols());
    }

    @Override
    public void generateTitleField() {
        log.info("Setting title field for PDF");
        resource.setBaseY(resource.getPageHeight());

        PdfTitleField titleField = (PdfTitleField) this.pdfFieldBuilders.get(PdfTitleField.TITLE_TYPE).buildField(
                PdfTitleFieldBuildingBlock.builder()
                        .text(resource.getDocumentTitle())
                        .build()
        );
        pdfDocumentContent.setTitleField(titleField);
        resource.setBaseY(resource.getBaseY() - titleField.getHeight());
    }

    @Override
    public void generatePdfFields() {
        log.info("Generating PDF fields from data fields.");
        lastX = Integer.MAX_VALUE;
        lastY = 0;

        this.dataGroups = dataService.getDataGroups(taskId, resource.getTextLocale(), userService.getLoggedOrSystem()).getData();

        dataGroups.forEach(dataGroup -> {
            generatePdfDataGroupField(dataGroup);
            if (dataGroup.getParentTaskRefId() == null) {
                refreshGrid(dataGroup);
                generatePdfFormFields(dataGroup);
                this.lastX = Integer.MAX_VALUE;
            }
        });
        pdfDocumentContent.getPdfFormFields().sort(Comparator.comparing(PdfField::getOriginalBottomY));
    }

    private void generatePdfFormFields(DataGroup dataGroup) {
        Collection<DataRef> dataRefs = dataGroup.getDataRefs().values();
        if (isGridLayout(dataGroup)) {
            dataRefs = dataRefs.stream().sorted(Comparator.<DataRef, Integer>comparing(f -> f.getLayout().getY()).thenComparing(f -> f.getLayout().getX())).collect(Collectors.toList());
        }
        dataRefs.forEach(dataRef -> {
            Field<?> field = dataRef.getField();
            if (field.getType().equals(DataType.TASK_REF)) {
                Optional<DataGroup> taskRefGroup = this.dataGroups.stream().filter(dg -> Objects.equals(dg.getParentTaskRefId(), field.getStringId())).findFirst();
                taskRefGroup.ifPresent(this::generatePdfFormFields);
            } else {
                generatePdfFormField(dataGroup, dataRef);
            }
        });
    }

    private boolean isGridLayout(DataGroup dataGroup) {
        return dataGroup.getLayout() != null && dataGroup.getLayout().getType() != null && dataGroup.getLayout().getType().equals(LayoutType.GRID);
    }

    @Override
    public void correctFieldsPosition() {
        log.info("Correcting field positions for correct export to PDF.");
        pdfDocumentContent.getPdfFormFields().forEach(pdfField -> {
            if (pdfField.isChangedSize()) {
            pdfField.setBottomY(updateBottomY(pdfField));
            changedPdfFields.push(pdfField);
            }
            shiftFields(pdfField);
        });

        while (!changedPdfFields.empty()) {
            PdfField<?> pdfField = changedPdfFields.pop();
            if (pdfField.isChangedSize()) {
                shiftFields(pdfField);
            }
            if (pdfField.isChangedPosition()) {
                shiftFields(pdfField);
            }
        }
    }

    private void generatePdfFormField(DataGroup dataGroup, DataRef dataRef) {
        Field<?> field = dataRef.getField();
        if (isNotHidden(field, dataGroup.getParentTransitionId()) && isNotExcluded(field.getStringId())) {
            PdfField<?> pdfField = createPdfField(dataGroup, dataRef);
            if (pdfField == null) {
                return;
            }
            pdfDocumentContent.addFormField(pdfField);
        }
    }

    private PdfField<?> createPdfField(DataGroup dataGroup, DataRef field) {
        PdfFieldBuilder<?> builder = pdfFieldBuilders.get(PdfGeneratorUtils.getCombinedTypeComponent(field));
        if (builder == null) {
            return null;
        }
        PdfField<?> pdfField = builder.buildField(PdfFormFieldBuildingBlock.builder()
                .dataGroup(dataGroup)
                .dataRef(field)
                .lastX(lastX)
                .lastY(lastY)
                .build()
        );
        updateLastCoordinates(builder.getLastX(), builder.getLastY());
        return pdfField;
    }

    private void updateLastCoordinates(int lastX, int lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
    }

    private int updateBottomY(PdfField<?> pdfField) {
        return PdfFieldBuilder.countBottomPosY(pdfField, resource);
    }

    private void shiftFields(PdfField<?> currentField) {
        pdfDocumentContent.getPdfFormFields().forEach(field -> {
            if (currentField != field) {
                shiftField(currentField, field);
            }
        });
    }

    private void shiftField(PdfField<?> currentField, PdfField<?> fieldBelow) {
        int belowTopY, cFieldBottomY;
        belowTopY = fieldBelow.getTopY();
        cFieldBottomY = currentField.getBottomY();
        if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY < belowTopY)) {
            shiftDown(belowTopY, cFieldBottomY, fieldBelow, resource);
        }
    }

    private void generatePdfDataGroupField(DataGroup dataGroup) {
        PdfField<?> dgField;
        if (dataGroup != null && dataGroup.getTitle() != null) {
            PdfDataGroupFieldBuilder builder = (PdfDataGroupFieldBuilder) pdfFieldBuilders.get(PdfDataGroupField.DATA_GROUP_TYPE);
            dgField = builder.buildField(PdfDataGroupFieldBuildingBlock.builder()
                    .importId(dataGroup.getImportId())
                    .lastX(lastX)
                    .lastY(lastY)
                    .y(lastY)
                    .title(dataGroup.getTitle())
                    .build());
            pdfDocumentContent.addFormField(dgField);
            updateLastCoordinates(builder.getLastX(), builder.getLastY());
        }
    }

    private void shiftDown(int belowTopY, int cFieldBottomY, PdfField<?> fieldBelow, PdfResource resource) {
        int currentDiff;
        currentDiff = cFieldBottomY - belowTopY + resource.getPadding();
        fieldBelow.setTopY(belowTopY + currentDiff);
        fieldBelow.setBottomY(fieldBelow.getBottomY() + currentDiff);
        fieldBelow.setChangedPosition(true);
        if (!changedPdfFields.contains(fieldBelow)) {
            changedPdfFields.push(fieldBelow);
        }
    }

    private boolean isCoveredByDataGroup(PdfField<?> currentField, PdfField<?> fieldBelow) {
        return currentField instanceof PdfDataGroupField && currentField.getOriginalTopY() >= fieldBelow.getOriginalTopY();
    }

    private boolean isCoveredByDataField(PdfField<?> currentField, PdfField<?> fieldBelow) {
        return currentField.getOriginalBottomY() > fieldBelow.getOriginalTopY();
    }

    private void refreshGrid(DataGroup dataGroup) {
        log.info("Refreshing grid for data group in PDF...");
        int cols = resource.getOriginalCols();
        if (dataGroup.getLayout() != null && dataGroup.getLayout().getCols() != null) {
            cols = dataGroup.getLayout().getCols();
        }
        resource.setFormGridCols(cols);
        resource.updateProperties();
    }

    private boolean isNotHidden(Field<?> field, String transitionId) {
        DataFieldBehavior fieldBehavior = field.getBehaviors().get(transitionId);
        if (fieldBehavior == null) {
            return true;
        }
        return fieldBehavior.getBehavior() != FieldBehavior.HIDDEN;
    }

    private boolean isNotExcluded(String fieldId) {
        return !excludedFields.contains(fieldId);
    }
}
