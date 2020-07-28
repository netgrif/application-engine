package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedEnumerationField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedMultichoiceField;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class that represents data that needs to be exported to PDF
 */
@Slf4j
@Service
public class DataConverter implements IDataConverter {

    @Autowired
    private PdfResource resource;

    @Getter
    @Setter
    private Map<String, DataGroup> dataGroups;

    @Getter
    @Setter
    private Map<String, DataField> dataSet;

    @Getter
    @Setter
    private List<PdfField> pdfFields;

    private Stack<PdfField> changedPdfFields;

    private int lastX, lastY;

    /**
     * Generates title field for PDF
     *
     * @param title text of title
     */
    @Override
    public void generateTitleField(String title) {
        log.info("Setting title field for PDF");

        resource.setBaseY(resource.getPageHeight() - resource.getMarginTop());
        pdfFields = new ArrayList<>();
        changedPdfFields = new Stack<>();

        PdfField titleField = new PdfField("titleField", 0, 0, resource.getPageDrawableWidth(),
                resource.getFormGridRowHeight(), title, false);

        titleField.setOriginalBottomY(countBottomPosY(titleField));
        titleField.countMultiLineHeight(resource.getFontTitleSize(), resource);
        titleField.setBottomY(countBottomPosY(titleField));

        pdfFields.add(titleField);
        resource.setBaseY(resource.getBaseY() - titleField.getBottomY());
    }

    /**
     * Creates list of FieldParam objects that allows to manipulate easier with data and sorts by their vertical
     * order in PDF
     */
    @Override
    public void generatePdfFields() {
        log.info("Generating PDF fields from data fields.");

        lastX = Integer.MAX_VALUE;
        lastY = 0;
        dataGroups.forEach((dataGroupId, dataGroup) ->
                dataGroup.getFields().getContent().forEach(field -> {
                            generateField(dataGroup, field);
                        }
                ));
        Collections.sort(pdfFields);
    }

    protected void generateField(DataGroup dataGroup, LocalisedField field) {
        if (field.getBehavior().get("hidden") == null) {
            switch (field.getType()) {
                case ENUMERATION:
                    pdfFields.add(createEnumField(dataGroup, (LocalisedEnumerationField) field));
                    break;
                case MULTICHOICE:
                    pdfFields.add(createMultiChoiceField(dataGroup, (LocalisedMultichoiceField) field));
                    break;
                default:
                    pdfFields.add(createPdfTextField(dataGroup, field));
                    break;
            }
        }
    }

    /**
     * Generates data group field for PDF
     */
    @Override
    public void generatePdfDataGroups() {
        log.info("Generating PDF field from data group titles.");

        List<PdfField> dgFields = new ArrayList<>();
        DataGroup currentDg = null;
        for (PdfField pdfField : pdfFields) {
            if (pdfField.getDataGroup() != null && pdfField.getDataGroup().getTitle() != null && pdfField.getDataGroup() != currentDg) {
                currentDg = pdfField.getDataGroup();
                PdfField dgField = createPdfDgField(currentDg, pdfField);
                dgFields.add(dgField);
            }
        }
        pdfFields.addAll(dgFields);
        Collections.sort(pdfFields);
    }

    /**
     * Creates PdfField object from input field
     *
     * @param dataGroup data group that contains current field
     * @param field     field that is currently converted to PdfField
     * @return newly created PdfField object
     */
    protected PdfField createPdfTextField(DataGroup dataGroup, LocalisedField field) {
        List<String> value = new ArrayList<>();

        switch (field.getType()) {
            case DATE:
            case DATETIME:
                value.add(formatDateTime(field));
                break;
            default:
                value.add(dataSet.get(field.getStringId()).getValue() != null ? dataSet.get(field.getStringId()).getValue().toString() : "");
                break;
        }

        PdfField pdfField = new PdfField(field.getStringId(), dataGroup, field.getType(), field.getName(), value, null);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    /**
     * Creates PdfField object from input enumeration field
     *
     * @param dataGroup data group that contains current field
     * @param field     field that is currently converted to PdfField
     * @return newly created PdfField object
     */
    protected PdfField createEnumField(DataGroup dataGroup, LocalisedEnumerationField field) {
        List<String> choices;
        List<String> values = new ArrayList<>();
        choices = field.getChoices();
        if (dataSet.get(field.getStringId()).getValue() != null) {
            values.add(dataSet.get(field.getStringId()).getValue().toString());
        }
        PdfField pdfField = new PdfField(field.getStringId(), dataGroup, field.getType(), field.getName(), values, choices);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    /**
     * Creates PdfField object from input multi choice field
     *
     * @param dataGroup data group that contains current field
     * @param field     field that is currently converted to PdfField
     * @return newly created PdfField object
     */
    protected PdfField createMultiChoiceField(DataGroup dataGroup, LocalisedMultichoiceField field) {
        List<String> choices;
        List<String> values = new ArrayList<>();
        choices = field.getChoices();
        if (dataSet.get(field.getStringId()).getValue() != null) {
            for (I18nString value : (List<I18nString>) dataSet.get(field.getStringId()).getValue()) {
                values.add(value.toString());
            }
        }
        PdfField pdfField = new PdfField(field.getStringId(), dataGroup, field.getType(), field.getName(), values, choices);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, resource.getFontLabelSize());
        return pdfField;
    }

    /**
     * Creates PdfField object from input data group
     *
     * @param dataGroup data group that is currently converted to PdfField
     * @param pdfField  PDF field that contains data about data group
     * @return newly created PdfField object
     */
    protected PdfField createPdfDgField(DataGroup dataGroup, PdfField pdfField) {
        PdfField dgField = new PdfField(dataGroup.getImportId(), pdfField.getLayoutX(), pdfField.getLayoutY(),
                pdfField.getWidth(), resource.getLineHeight(), dataGroup.getTitle().toString(), true);
        setFieldPositions(dgField, resource.getFontGroupSize());
        return dgField;
    }

    private void setFieldParams(DataGroup dg, LocalisedField field, PdfField pdfField) {
        pdfField.setLayoutX(countFieldLayoutX(dg, field));
        pdfField.setLayoutY(countFieldLayoutY(dg, field));
        pdfField.setWidth(countFieldWidth(dg, field));
        pdfField.setHeight(countFieldHeight(field));
    }

    private void setFieldPositions(PdfField pdfField, int fontSize) {
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField));
        pdfField.setTopY(countTopPosY(pdfField));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField));
        pdfField.setBottomY(countBottomPosY(pdfField));
        pdfField.countMultiLineHeight(fontSize, resource);
    }

    /**
     * Check parameters of FieldParam objects and calls correction functions, when a field's cannot be drawn into PDF
     * correctly
     */
    public void correctFieldsPosition() {
        pdfFields.forEach(pdfField -> {
            if (pdfField.isChangedSize()) {
                pdfField.setBottomY(countBottomPosY(pdfField));
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

    /**
     * Counts height difference of currentField when too large text is needed to be converted to pdf and
     * shifts fields below by that difference
     *
     * @param currentField FieldParam object
     */
    private void shiftFieldsBelow(PdfField currentField) {
        pdfFields.forEach(fieldBelow -> {
            if (!currentField.equals(fieldBelow)) {
                shiftField(currentField, fieldBelow);
            }
        });
    }

    protected void shiftField(PdfField currentField, PdfField fieldBelow){
        int belowTopY, cFieldBottomY;
        belowTopY = fieldBelow.getTopY();
        cFieldBottomY = currentField.getBottomY();
        if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY > belowTopY)) {
            setNewPositions(belowTopY, cFieldBottomY, fieldBelow);
        }
    }

    private void setNewPositions(int belowTopY, int cFieldBottomY, PdfField fieldBelow) {
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

    /**
     * Counts X position of a field on the grid from XML
     *
     * @param dataGroup a dataGroup that contains field
     * @param field     a field that is being converted to FieldParam
     * @return X position of a field in an abstract grid
     */
    private int countFieldLayoutX(DataGroup dataGroup, LocalisedField field) {
        int x = 0;
        if (field.getLayout() != null) {
            x = field.getLayout().getX();
        } else if (dataGroup.getStretch() == null || !dataGroup.getStretch()) {
            lastX = (lastX == 0 ? 2 : 0);
            x = lastX;
        }
        return x;
    }

    /**
     * Counts Y position of field on the grid from XML
     *
     * @param dataGroup a dataGroup that contains field
     * @param field     a field that is being converted to FieldParam
     * @return Y position of a field in an abstract grid
     */
    private int countFieldLayoutY(DataGroup dataGroup, LocalisedField field) {
        int y;
        if (field.getLayout() != null) {
            y = field.getLayout().getY();
        } else if (dataGroup.getStretch() != null && dataGroup.getStretch()) {
            y = ++lastY;
        } else {
            lastY = (lastX == 0 ? ++lastY : lastY);
            y = lastY;
        }
        return y;
    }

    /**
     * Counts the X coordinate of the most upper part of the field in the PDF document
     *
     * @return
     */
    public int countPosX(PdfField field) {
        return (field.getLayoutX() * resource.getFormGridColWidth() + resource.getPadding());
    }

    /**
     * Counts the Y coordinate of the most upper part of the field in the PDF document
     *
     * @return
     */
    public int countTopPosY(PdfField field) {
        return (field.getLayoutY() * resource.getFormGridRowHeight()) + resource.getPadding();
    }

    /**
     * Counts the Y coordinate of the most lower part of the field in the PDF document
     *
     * @return
     */
    public int countBottomPosY(PdfField field) {
        return (field.getLayoutY() * resource.getFormGridRowHeight()) + field.getHeight() + resource.getPadding();
    }

    /**
     * Generates array of strings in case the text is too long to fit into one line in PDF.
     * Each element represents a single line in final PDF
     *
     * @param values
     * @param maxLineLength
     * @return result
     */
    public static List<String> generateMultiLineText(List<String> values, float maxLineLength) {
        StringTokenizer tokenizer;
        StringBuilder output;
        List<String> result = new ArrayList<>();
        int lineLen = 1;

        for (String value : values) {
            tokenizer = new StringTokenizer(value, " ");
            output = new StringBuilder(value.length());
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();

                if (lineLen + word.length() > maxLineLength) {
                    output.append("\n");
                    lineLen = 0;
                }
                output.append(word + " ");
                lineLen += word.length() + 1;
            }
            result.addAll(Arrays.asList(output.toString().split("\n")));
        }
        return result;
    }

    /**
     * Counts field width for drawing the data field to PDF
     *
     * @param dataGroup that the field is part of
     * @param field     that is the width counted for
     * @return width of rectangle that will be drawn to PDF
     */
    private int countFieldWidth(DataGroup dataGroup, LocalisedField field) {
        if (field.getLayout() != null) {
            return field.getLayout().getCols() * resource.getFormGridColWidth() - resource.getPadding();
        } else {
            return (dataGroup.getStretch() != null && dataGroup.getStretch() ?
                    (resource.getFormGridColWidth() * resource.getFormGridCols())
                    : (resource.getFormGridColWidth() * resource.getFormGridCols() / 2)) - resource.getPadding();
        }
    }

    /**
     * Counts field width for drawing the data field to PDF
     *
     * @param field that is the width counted for
     * @return height of rectangle that will be drawn to PDF
     */
    private int countFieldHeight(LocalisedField field) {
        if (field.getLayout() != null) {
            return field.getLayout().getRows() * resource.getFormGridRowHeight() - resource.getPadding();
        } else {
            return resource.getFormGridRowHeight() - resource.getPadding();
        }
    }

    private String formatDateTime(LocalisedField field) {
        if (dataSet.get(field.getStringId()).getValue() != null) {
            return new SimpleDateFormat("dd-MM-yyyy").format(dataSet.get(field.getStringId()).getValue());
        } else {
            return "";
        }
    }
}
