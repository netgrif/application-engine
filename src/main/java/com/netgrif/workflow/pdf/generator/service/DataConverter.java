package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.config.PdfProperties;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedEnumerationField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedMultichoiceField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Class that represents data that needs to be exported to PDF
 */
@Service
public class DataConverter extends PdfProperties implements IDataConverter {

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
     * @param title text of title
     */
    @Override
    public void generateTitleField(String title){
        BASE_Y = PAGE_HEIGHT - MARGIN_TOP;
        pdfFields = new ArrayList<>();
        changedPdfFields = new Stack<>();

        PdfField titleField = new PdfField("titleField", 0, 0, PAGE_DRAWABLE_WIDTH,
                FORM_GRID_ROW_HEIGHT,  title, false);

        titleField.setOriginalBottomY(countBottomPosY(titleField));
        titleField.countMultiLineHeight(FONT_TITLE_SIZE);
        titleField.setBottomY(countBottomPosY(titleField));

        pdfFields.add(titleField);
        BASE_Y = BASE_Y - titleField.getBottomY();
    }

    /**
     * Creates list of FieldParam objects that allows to manipulate easier with data and sorts by their vertical
     * order in PDF
     */
    @Override
    public void generatePdfFields(){
        lastX = Integer.MAX_VALUE;
        lastY = 0;
        for(Map.Entry<String,DataGroup> entry : dataGroups.entrySet()) {
            for(LocalisedField field : entry.getValue().getFields().getContent()) {
                if(field.getBehavior().get("hidden") == null) {
                    if(field.getType().equals(FieldType.ENUMERATION)){
                        pdfFields.add(createEnumField(entry.getValue(), (LocalisedEnumerationField) field));
                    }else if(field.getType().equals(FieldType.MULTICHOICE)){
                        pdfFields.add(createMultiChoiceField(entry.getValue(), (LocalisedMultichoiceField) field));
                    }else{
                        pdfFields.add(createPdfField(entry.getValue(), field));
                    }
                }
            }
        }
        Collections.sort(pdfFields);
    }

    /**
     * Generates data group field for PDF
     */
    @Override
    public void generatePdfDataGroups(){
        DataGroup currentDg = null;
        List<PdfField> dgFields = new ArrayList<>();
        for(PdfField pdfField : pdfFields){
            if(pdfField.getDataGroup() != null && pdfField.getDataGroup().getTitle() != null && pdfField.getDataGroup() != currentDg){
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
     * @param dataGroup data group that contains current field
     * @param field field that is currently converted to PdfField
     * @return newly created PdfField object
     */
    private PdfField createPdfField(DataGroup dataGroup, LocalisedField field){
        List<String> value = new ArrayList<>();
        value.add(dataSet.get(field.getStringId()).getValue() != null ? dataSet.get(field.getStringId()).getValue().toString() : "");
        PdfField pdfField = new PdfField(field.getStringId(), dataGroup, field.getType(), field.getName(), value, null);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, FONT_LABEL_SIZE);
        return pdfField;
    }

    /**
     * Creates PdfField object from input enumeration field
     * @param dataGroup data group that contains current field
     * @param field field that is currently converted to PdfField
     * @return newly created PdfField object
     */
    private PdfField createEnumField(DataGroup dataGroup, LocalisedEnumerationField field){
        List<String> choices;
        List<String> values = new ArrayList<>();
        choices = field.getChoices();
        if(dataSet.get(field.getStringId()).getValue() != null){
            values.add(dataSet.get(field.getStringId()).getValue().toString());
        }
        PdfField pdfField = new PdfField(field.getStringId(), dataGroup, field.getType(), field.getName(), values, choices);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, FONT_LABEL_SIZE);
        return pdfField;
    }

    /**
     * Creates PdfField object from input multi choice field
     * @param dataGroup data group that contains current field
     * @param field field that is currently converted to PdfField
     * @return newly created PdfField object
     */
    private PdfField createMultiChoiceField(DataGroup dataGroup, LocalisedMultichoiceField field){
        List<String> choices;
        List<String> values = new ArrayList<>();
        choices = field.getChoices();
        if(dataSet.get(field.getStringId()).getValue() != null){
            for(I18nString value : (List<I18nString>) dataSet.get(field.getStringId()).getValue()){
                values.add(value.toString());
            }
        }
        PdfField pdfField = new PdfField(field.getStringId(), dataGroup, field.getType(), field.getName(), values, choices);
        setFieldParams(dataGroup, field, pdfField);
        setFieldPositions(pdfField, FONT_LABEL_SIZE);
        return pdfField;
    }

    /**
     * Creates PdfField object from input data group
     * @param dataGroup data group that is currently converted to PdfField
     * @param pdfField PDF field that contains data about data group
     * @return newly created PdfField object
     */
    private PdfField createPdfDgField(DataGroup dataGroup, PdfField pdfField){
        PdfField dgField = new PdfField(dataGroup.getImportId(), pdfField.getLayoutX(), pdfField.getLayoutY(),
                pdfField.getWidth(), LINE_HEIGHT, dataGroup.getTitle().toString(), true);
        setFieldPositions(dgField, FONT_GROUP_SIZE);
        return dgField;
    }

    private void setFieldParams(DataGroup dg, LocalisedField field, PdfField pdfField){
        pdfField.setLayoutX(countFieldLayoutX(dg, field));
        pdfField.setLayoutY(countFieldLayoutY(dg, field));
        pdfField.setWidth(countFieldWidth(dg, field));
        pdfField.setHeight(countFieldHeight(field));
    }

    private void setFieldPositions(PdfField pdfField, int fontSize){
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField));
        pdfField.setTopY(countTopPosY(pdfField));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField));
        pdfField.setBottomY(countBottomPosY(pdfField));
        pdfField.countMultiLineHeight(fontSize);
    }

    /**
     * Check parameters of FieldParam objects and calls correction functions, when a field's cannot be drawn into PDF
     * correctly
     */
    public void correctFieldsPosition(){
        for(PdfField pdfField : pdfFields){
            if(pdfField.isChangedSize()) {
                pdfField.setBottomY(countBottomPosY(pdfField));
                changedPdfFields.push(pdfField);
            }
        }
        while(!changedPdfFields.empty()){
            PdfField pdfField = changedPdfFields.pop();
            if(pdfField.isChangedSize()){
                shiftFieldsBelow(pdfField);
            }
            if(pdfField.isChangedPosition()){
                shiftFieldsBelow(pdfField);
            }
        }
    }

    /**
     * Counts height difference of currentField when too large text is needed to be converted to pdf and
     * shifts fields below by that difference
     * @param currentField FieldParam object
     */
    private void shiftFieldsBelow(PdfField currentField){
        int belowTopY, cFieldBottomY;

        for(PdfField fieldBelow : pdfFields){
            if(!currentField.equals(fieldBelow)) {
                belowTopY = fieldBelow.getTopY();
                cFieldBottomY = currentField.getBottomY();
                if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY > belowTopY)) {
                    setNewPositions(belowTopY, cFieldBottomY, fieldBelow);
                }
            }
        }
    }

    private void setNewPositions( int belowTopY, int cFieldBottomY, PdfField fieldBelow) {
        int currentDiff;
        currentDiff = cFieldBottomY - belowTopY + PADDING;
        fieldBelow.setTopY(belowTopY + currentDiff);
        fieldBelow.setBottomY(fieldBelow.getBottomY() + currentDiff);
        fieldBelow.setChangedPosition(true);
        if(!changedPdfFields.contains(fieldBelow)){
            changedPdfFields.push(fieldBelow);
        }
    }

    private boolean isCoveredByDataGroup(PdfField currentField, PdfField fieldBelow){
        return currentField.isDgField() && currentField.getOriginalTopY() <= fieldBelow.getOriginalTopY();
    }

    private boolean isCoveredByDataField(PdfField currentField, PdfField fieldBelow){
        return currentField.getOriginalBottomY() < fieldBelow.getOriginalTopY();
    }

    /**
     * Counts X position of a field on the grid from XML
     * @param dataGroup a dataGroup that contains field
     * @param field a field that is being converted to FieldParam
     * @return X position of a field in an abstract grid
     */
    private int countFieldLayoutX(DataGroup dataGroup, LocalisedField field){
        int x;
        if(field.getLayout() != null){
            x = field.getLayout().getX();
        }else if(dataGroup.getStretch() != null && dataGroup.getStretch()){
            x = 0;
        }else{
            lastX = (lastX == 0 ? 2 : 0);
            x = lastX;
        }
        return x;
    }

    /**
     * Counts Y position of field on the grid from XML
     * @param dataGroup a dataGroup that contains field
     * @param field a field that is being converted to FieldParam
     * @return Y position of a field in an abstract grid
     */
    private int countFieldLayoutY(DataGroup dataGroup, LocalisedField field){
        int y;
        if(field.getLayout() != null){
            y = field.getLayout().getY();
        }else if(dataGroup.getStretch() != null && dataGroup.getStretch()){
            y = ++lastY;
        }else{
            lastY = (lastX == 0 ? ++lastY : lastY);
            y = lastY;
        }
        return y;
    }

    /**
     * Counts the X coordinate of the most upper part of the field in the PDF document
     * @return
     */
    public int countPosX(PdfField field){
        return (field.getLayoutX() * FORM_GRID_COL_WIDTH + PADDING);
    }

    /**
     * Counts the Y coordinate of the most upper part of the field in the PDF document
     * @return
     */
    public int countTopPosY(PdfField field){
        return (field.getLayoutY() * FORM_GRID_ROW_HEIGHT) + PADDING;
    }

    /**
     * Counts the Y coordinate of the most lower part of the field in the PDF document
     * @return
     */
    public int countBottomPosY(PdfField field){
        return (field.getLayoutY() * FORM_GRID_ROW_HEIGHT) + field.getHeight() + PADDING;
    }

    /**
     * Generates array of strings in case the text is too long to fit into one line in PDF.
     * Each element represents a single line in final PDF
     * @param values
     * @param maxLineLength
     * @return result
     */
    public static List<String> generateMultiLineText(List<String> values, float maxLineLength){
        StringTokenizer tokenizer;
        StringBuilder output;
        List<String> result = new ArrayList<>();
        int lineLen = 1;

        for(String value : values) {
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
     * @param dataGroup that the field is part of
     * @param field that is the width counted for
     * @return width of rectangle that will be drawn to PDF
     */
    private int countFieldWidth(DataGroup dataGroup, LocalisedField field){
        if(field.getLayout() != null) {
            return field.getLayout().getCols() * FORM_GRID_COL_WIDTH - PADDING;
        }else{
            return (dataGroup.getStretch() != null && dataGroup.getStretch() ? (FORM_GRID_COL_WIDTH * FORM_GRID_COLS) : (FORM_GRID_COL_WIDTH * FORM_GRID_COLS / 2)) - PADDING;
        }
    }

    /**
     * Counts field width for drawing the data field to PDF
     * @param field that is the width counted for
     * @return height of rectangle that will be drawn to PDF
     */
    private int countFieldHeight(LocalisedField field){
        if(field.getLayout() != null) {
            return field.getLayout().getRows() * FORM_GRID_ROW_HEIGHT - PADDING;
        }else{
            return FORM_GRID_ROW_HEIGHT - PADDING;
        }
    }
}
