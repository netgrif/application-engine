package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfProperties;
import com.netgrif.workflow.pdf.generator.service.DataConverter;
import com.netgrif.workflow.pdf.generator.service.PdfDrawer;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds information about fields that will be exported to PDF
 * */
public class PdfField extends PdfProperties implements Comparable<PdfField>{

    @Getter
    @Setter
    private String fieldId;

    @Getter
    @Setter
    private DataGroup dataGroup = null;

    @Getter
    @Setter
    private FieldType type;

    @Getter
    @Setter
    private String label;

    @Getter
    @Setter
    private String value = null;

    @Getter
    @Setter
    private int layoutX;

    @Getter
    @Setter
    private int layoutY;

    @Getter
    @Setter
    int x;

    @Getter
    @Setter
    int originalTopY;

    @Getter
    @Setter
    int topY;

    @Getter
    @Setter
    Integer originalBottomY;

    @Getter
    @Setter
    int bottomY;

    @Getter
    @Setter
    private int width;

    @Getter
    @Setter
    private int height;

    @Getter
    @Setter
    private boolean changedSize = false;

    @Getter
    @Setter
    private boolean changedPosition = false;

    @Getter
    @Setter
    private boolean dgField;


    public PdfField(String fieldId, DataGroup dataGroup, int layoutX, int layoutY, int width, int originalHeight,
                    FieldType type, String label, String value){
        this.fieldId = fieldId;
        this.dataGroup = dataGroup;
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.width = width;
        this.height = originalHeight;
        this.type = type;
        this.label = label;
        this.value = value;
        this.dgField = false;
    }

    public PdfField(String fieldId, int layoutX, int layoutY, int width, int originalHeight, String label){
        this.fieldId = fieldId;
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.width = width;
        this.height = originalHeight;
        this.label = label;
        this.dgField = true;
    }

    /**
     * Counts and changes fields default height in case there are multiple lines and text will not fit into default size
     */
    public void countMultiLineHeight(){
        int maxLabelLineLength = PdfDrawer.getMaxLabelLineSize(this.width);
        int maxValueLineLength = PdfDrawer.getMaxValueLineSize(this.width);
        int multiLineHeight = 0;

        String[] splitLabel = DataConverter.generateMultiLineText(this.label, maxLabelLineLength);
        multiLineHeight += splitLabel.length * LINE_HEIGHT + PADDING;

        if(this.value != null){
            String[] splitText = DataConverter.generateMultiLineText(this.value, maxValueLineLength);
            multiLineHeight += splitText.length * LINE_HEIGHT + PADDING;
        }

        this.changedSize = changeHeight(multiLineHeight);
    }

    /**
     * Changes the field's height for new multiLineHeight when the too long text requires more space
     * @param multiLineHeight the new height of field
     * @return boolean value, whether the change was executed or not
     */
    private boolean changeHeight(int multiLineHeight){
        if(multiLineHeight > this.height){
            this.height = multiLineHeight;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int compareTo(PdfField pdfField) {
        return this.getOriginalBottomY().compareTo(pdfField.getOriginalBottomY());
    }
}
