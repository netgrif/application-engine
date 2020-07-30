package com.netgrif.workflow.pdf.generator.domain;

import com.netgrif.workflow.pdf.generator.config.PdfProperties;
import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.service.DataConverter;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Class that holds information about fields that will be exported to PDF
 */
@Data
public class PdfField extends PdfProperties implements Comparable<PdfField> {

    private String fieldId;

    private DataGroup dataGroup = null;

    private FieldType type = null;

    private String label;

    private List<String> values = null;

    private List<String> choices = null;

    private int layoutX;

    private int layoutY;

    private int x;

    private int originalTopY;

    private int topY;

    private Integer originalBottomY;

    private int bottomY;

    private int width;

    private int height;

    private boolean changedSize = false;

    private boolean changedPosition = false;

    private boolean dgField;

    public PdfField(String fieldId, DataGroup dataGroup, FieldType type, String label, List<String> values, List<String> choices) {
        this.fieldId = fieldId;
        this.dataGroup = dataGroup;
        this.type = type;
        this.label = label;
        this.values = values;
        this.choices = choices;
        this.dgField = false;
    }

    public PdfField(String fieldId, int layoutX, int layoutY, int width, int originalHeight,
                    String label, boolean dgField) {
        this.fieldId = fieldId;
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.width = width;
        this.height = originalHeight;
        this.label = label;
        this.dgField = dgField;
    }

    public PdfField(String fieldId, String label, List<String> values, List<String> choices, FieldType type, int x, int bottomY, int width, int height) {
        this.fieldId = fieldId;
        this.label = label;
        this.values = values;
        this.choices = choices;
        this.type = type;
        this.x = x;
        this.bottomY = bottomY;
        this.width = width;
        this.height = height;
    }

    /**
     * Counts and changes fields default height in case there are multiple lines and text will not fit into default size
     *
     * @param fontSize size of font
     */
    public void countMultiLineHeight(int fontSize, PdfResource resource) {
        int padding = resource.getPadding();
        int lineHeight = resource.getLineHeight();
        int maxLabelLineLength = getMaxLabelLineSize(this.width, fontSize, padding);
        int maxValueLineLength = getMaxValueLineSize(this.width - 3 * padding, resource.getFontValueSize(), padding);
        int multiLineHeight = 0;

        List<String> splitLabel = DataConverter.generateMultiLineText(Collections.singletonList(this.label), maxLabelLineLength);
        multiLineHeight += splitLabel.size() * lineHeight + padding;

        if (this.choices != null) {
            List<String> splitText = DataConverter.generateMultiLineText(this.choices, maxValueLineLength);
            multiLineHeight += splitText.size() * lineHeight + padding;
        } else if (this.values != null) {
            List<String> splitText = DataConverter.generateMultiLineText(this.values, maxValueLineLength);
            multiLineHeight += splitText.size() * lineHeight + padding;
        }

        this.changedSize = changeHeight(multiLineHeight);
    }

    /**
     * Changes the field's height for new multiLineHeight when the too long text requires more space
     *
     * @param multiLineHeight the new height of field
     * @return boolean value, whether the change was executed or not
     */
    private boolean changeHeight(int multiLineHeight) {
        if (multiLineHeight <= this.height) {
            return false;
        }
        this.height = multiLineHeight;
        return true;
    }

    @Override
    public int compareTo(PdfField pdfField) {
        return this.getOriginalBottomY().compareTo(pdfField.getOriginalBottomY());
    }

    private int getMaxLabelLineSize(int fieldWidth, int fontSize, int padding) {
        return (int) ((fieldWidth - padding) * marginMultiplier / fontSize);
    }

    private int getMaxValueLineSize(int fieldWidth, int fontSize, int padding) {
        return (int) ((fieldWidth - padding) * marginMultiplier / fontSize);
    }
}
