package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.config.PdfProperties;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.workflow.domain.DataField;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField;
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

    public void countSpaceForTitle(String title){

    }

    /**
     * Creates list of FieldParam objects that allows to manipulate easier with data and sorts by their vertical
     * order in PDF
     */
    public void generatePdfFields(){
        pdfFields = new ArrayList<>();
        changedPdfFields = new Stack<>();
        lastX = Integer.MAX_VALUE;
        lastY = 0;

        for(Map.Entry<String,DataGroup> entry : dataGroups.entrySet()) {
            for(LocalisedField field : entry.getValue().getFields().getContent()) {
                pdfFields.add(createPdfField(entry.getValue(), field));
            }
        }
        pdfFields.sort(Collections.reverseOrder());
    }

    public void generatePdfDataGroups(){
        DataGroup currentDg = null;
        List<PdfField> dgFields = new ArrayList<>();
        for(PdfField pdfField : pdfFields){
            if(pdfField.getDataGroup().getTitle() != null && pdfField.getDataGroup() != currentDg){
                currentDg = pdfField.getDataGroup();
                PdfField dgField = createPdfDgField(currentDg, pdfField);
                dgFields.add(dgField);
            }
        }
        pdfFields.addAll(dgFields);
        pdfFields.sort(Collections.reverseOrder());
    }

    /**
     * Check parameters of FieldParam objects and calls correction functions, when a field's cannot be drawn into PDF
     * correctly
     */
    public void correctCoveringFields(){
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
        int maxDiff = 0, belowTopY, cFieldBottomY;

        for(PdfField fieldBelow : pdfFields){
            if(!currentField.equals(fieldBelow)) {
                belowTopY = fieldBelow.getTopY();
                cFieldBottomY = currentField.getBottomY();
                if ((isCoveredByDataField(currentField, fieldBelow) || isCoveredByDataGroup(currentField, fieldBelow)) && (cFieldBottomY < belowTopY)) {
                    maxDiff = setNewPositions(maxDiff, belowTopY, cFieldBottomY, fieldBelow);
                }
            }
        }
    }

    private boolean isCoveredByDataGroup(PdfField currentField, PdfField fieldBelow){
        return currentField.isDgField() && currentField.getOriginalTopY() >= fieldBelow.getOriginalTopY();
    }

    private boolean isCoveredByDataField(PdfField currentField, PdfField fieldBelow){
        return currentField.getOriginalBottomY() > fieldBelow.getOriginalTopY();
    }

    private int setNewPositions(int maxDiff, int belowTopY, int cFieldBottomY, PdfField fieldBelow) {
        int currentDiff;
        currentDiff = belowTopY - cFieldBottomY + PADDING;
        if(maxDiff < currentDiff){
            maxDiff = currentDiff;
        }
        fieldBelow.setTopY(belowTopY - maxDiff);
        fieldBelow.setBottomY(fieldBelow.getBottomY() - maxDiff);
        fieldBelow.setChangedPosition(true);
        if(!changedPdfFields.contains(fieldBelow)){
            changedPdfFields.push(fieldBelow);
        }
        return maxDiff;
    }

    private PdfField createPdfField(DataGroup dataGroup, LocalisedField field){
        int fieldWidth, fieldHeight, fieldLayoutX, fieldLayoutY;
        String value;

        fieldLayoutX = countFieldLayoutX(dataGroup, field);
        fieldLayoutY = countFieldLayoutY(dataGroup, field);
        fieldWidth = countFieldWidth(field, dataGroup);
        fieldHeight = countFieldHeight(field);
        value = dataSet.get(field.getStringId()).getValue().toString();

        PdfField dataPdfField = new PdfField(field.getStringId(), dataGroup, fieldLayoutX,
                fieldLayoutY, fieldWidth, fieldHeight, field.getType(), field.getName(), value);
        setFieldPositions(dataPdfField);
        return dataPdfField;
    }

    private PdfField createPdfDgField(DataGroup dataGroup, PdfField pdfField){
        PdfField dgField = new PdfField(dataGroup.getImportId(), pdfField.getLayoutX(), pdfField.getLayoutY(),
                pdfField.getWidth(), LINE_HEIGHT, dataGroup.getTitle().toString());
        setFieldPositions(dgField);
        return dgField;

    }

    private void setFieldPositions(PdfField pdfField){
        pdfField.setX(countPosX(pdfField));
        pdfField.setOriginalTopY(countTopPosY(pdfField));
        pdfField.setTopY(countTopPosY(pdfField));
        pdfField.setOriginalBottomY(countBottomPosY(pdfField));
        pdfField.setBottomY(countBottomPosY(pdfField));
        pdfField.countMultiLineHeight();
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
        return BASE_X + (field.getLayoutX() * FORM_GRID_COL_WIDTH);
    }

    /**
     * Counts the Y coordinate of the most upper part of the field in the PDF document
     * @return
     */
    public int countTopPosY(PdfField field){
        return BASE_Y - (field.getLayoutY() * FORM_GRID_ROW_HEIGHT) - PADDING;
    }

    /**
     * Counts the Y coordinate of the most lower part of the field in the PDF document
     * @return
     */
    public int countBottomPosY(PdfField field){
        return BASE_Y - (field.getLayoutY() * FORM_GRID_ROW_HEIGHT) - field.getHeight() - PADDING;
    }

    /**
     * Generates array of strings in case the text is too long to fit into one line in PDF.
     * Each element represents a single line in final PDF
     * @param text
     * @param maxLineLength
     * @return result
     */
    public static String[] generateMultiLineText(String text, float maxLineLength){
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        StringBuilder output = new StringBuilder(text.length());
        String[] result;
        int lineLen = 1;

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();

            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word + " ");
            lineLen += word.length() + 1;
        }
        result = output.toString().split("\n");
        return result;
    }

    /**
     * Counts field width for drawing the data field to PDF
     * @param field that is the width counted for
     * @param dataGroup that the field is part of
     * @return width of rectangle that will be drawn to PDF
     */
    private int countFieldWidth(LocalisedField field, DataGroup dataGroup){
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
