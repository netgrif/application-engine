package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfProperties;
import com.netgrif.workflow.pdf.generator.service.DataConverter;
import com.netgrif.workflow.pdf.generator.service.PdfDrawer;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * Class that holds information about fields that will be exported to PDF
 * */
@Data
public class PdfField extends PdfProperties implements Comparable<PdfField>{

    private String fieldId;

    private DataGroup dataGroup = null;

    public FieldType type = null;

    public String label;

    public List<String> values = null;

    public List<String> choices = null;

    private int layoutX;

    private int layoutY;

    public int x;

    private int originalTopY;

    private int topY;

    private Integer originalBottomY;

    public int bottomY;

    public int width;

    public int height;

    private boolean changedSize = false;

    private boolean changedPosition = false;

    private boolean dgField;

    public PdfField(String fieldId, DataGroup dataGroup, FieldType type, String label, List<String> values, List<String> choices){
        this.fieldId = fieldId;
        this.dataGroup = dataGroup;
        this.type = type;
        this.label = label;
        this.values = values;
        this.choices = choices;
        this.dgField = false;
    }

    public PdfField(String fieldId, int layoutX, int layoutY, int width, int originalHeight,
                    String label, boolean dgField){
        this.fieldId = fieldId;
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.width = width;
        this.height = originalHeight;
        this.label = label;
        this.dgField = dgField;
    }

    /**
     * Counts and changes fields default height in case there are multiple lines and text will not fit into default size
     * @param fontSize size of font
     */
    public void countMultiLineHeight(int fontSize){
        int maxLabelLineLength = PdfDrawer.getMaxLabelLineSize(this.width, fontSize);
        int maxValueLineLength = PdfDrawer.getMaxValueLineSize(this.width - 3 * PADDING);
        int multiLineHeight = 0;

        List<String> splitLabel = DataConverter.generateMultiLineText(Collections.singletonList(this.label), maxLabelLineLength);
        multiLineHeight += splitLabel.size() * LINE_HEIGHT + PADDING;

        if(this.choices != null){
            List<String> splitText = DataConverter.generateMultiLineText(this.choices, maxValueLineLength);
            multiLineHeight += splitText.size() * LINE_HEIGHT + PADDING;
        }else if(this.values != null){
            List<String> splitText = DataConverter.generateMultiLineText(this.values, maxValueLineLength);
            multiLineHeight += splitText.size() * LINE_HEIGHT + PADDING;
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
