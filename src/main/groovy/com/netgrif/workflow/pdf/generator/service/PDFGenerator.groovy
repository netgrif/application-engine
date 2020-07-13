package com.netgrif.workflow.pdf.generator.service

import com.netgrif.workflow.pdf.generator.data.TransitionFormData
import com.netgrif.workflow.petrinet.domain.DataGroup
import com.netgrif.workflow.petrinet.domain.dataset.FieldType
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField
import groovy.util.logging.Slf4j
import lombok.Getter
import lombok.Setter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Service

import java.awt.* 
/**
 * Generates PDF from the given transition form
 * */
@Slf4j
@Service
class PDFGenerator extends PDFGeneratorProperties implements IPDFGenerator {

    private boolean isEmptyPage = true

    private int currentX = marginLeft
    private int currentY = pageHeight - marginTop

    private int formGridCols = 4
    private int formGridRows = 25
    private int formGridColWidth = (pageDrawableWidth / formGridCols) as int
    private int formGridRowHeight = ((pageHeight - marginBottom - marginTop) / formGridRows) as int

    private PDPageContentStream contentStream
    private PDPage actualPage
    private PDPage lastPage

    @Getter
    @Setter
    PDDocument pdf

    private void constructPDFGenerator(){
        this.setPdf(new PDDocument())

        File fontTitleFile = new File(fontTitlePath)
        File fontLabelFile = new File(fontLabelPath)
        File fontValueFile = new File(fontValuePath)

        this.setTitleFont(PDType0Font.load(this.getPdf(), new FileInputStream(fontTitleFile), true))
        this.setLabelFont(PDType0Font.load(this.getPdf(), new FileInputStream(fontLabelFile), true))
        this.setValueFont(PDType0Font.load(this.getPdf(), new FileInputStream(fontValueFile), true))

        this.setCheckboxChecked(PDImageXObject.createFromFile(checkBoxCheckedPath, this.getPdf()))
        this.setCheckboxUnchecked(PDImageXObject.createFromFile(checkBoxUnCheckedPath, this.getPdf()))
        this.setRadioChecked(PDImageXObject.createFromFile(radioCheckedPath, this.getPdf()))
        this.setRadioUnchecked(PDImageXObject.createFromFile(radioUnCheckedPath, this.getPdf()))
    }

    @Override
    void convertCaseForm(Case formCase, String transitionId) {
        TransitionFormData transitionFormData = new TransitionFormData(formCase.petriNet.transitions[transitionId].dataGroups, formCase.dataSet)
        File result = transformRequestToPdf(transitionFormData)
    }

    private File transformRequestToPdf(TransitionFormData transitionFormData) {
        try {
            constructPDFGenerator()
            newPage()
            drawTransitionForm(transitionFormData)
            contentStream.close()
        } catch (Exception e) {
            log.info("Error", e)
        } finally {
            if (pdf == null)
                throw new IllegalArgumentException("[$pdf] is null  ")
            File out = new File("src/main/resources/out.pdf")
            if (!out.exists())
                out.createNewFile()
            pdf.save(out)
            pdf.close()
            contentStream = null
            return out
        }
    }



    /**
     * Parses the input data groups and data field to a PDF file and draws to a PDF file using corresponding
     * functions
     * */
    void drawTransitionForm(TransitionFormData transitionFormData) {
        int layoutX, layoutY, fieldWidth, fieldHeight
        Map<String, DataGroup> dataGroupMap = transitionFormData.getDataGroups()
        Map<String, DataField> dataSet = transitionFormData.getDataSet()

        //drawTitle()

        dataGroupMap.each { dataGroupId, dataGroup ->
            if(dataGroup.title != null){
                drawDataGroup(dataGroup.title as String)
            }
            dataGroup.fields.content.each { field ->
                layoutX = field.layout.x
                layoutY = field.layout.y
                fieldWidth = countFieldWidth(field, dataGroup)
                fieldHeight = countFieldHeight(field)

                switch (field.type) {
                    case FieldType.TEXT:
                        drawTextField(field, dataSet[field.stringId], layoutX, layoutY, fieldWidth, fieldHeight)
                        break
                }
            }
        }
    }

    /**
     * Creates new page whether it is a new document or just the bottom of current page is reached
     * */
    private void newPage() {
        if (contentStream != null) {
            contentStream.close()
        }
        lastPage = actualPage
        actualPage = new PDPage(pageSize)
        pdf.addPage(actualPage)
        contentStream = new PDPageContentStream(pdf, actualPage, PDPageContentStream.AppendMode.APPEND, true, true)
        isEmptyPage = true
    }

    /**
     * Moves the cursor to a new line
     * */
    private void newLine(float power = 2.2) {
        currentX = marginLeft
        currentY -= lineHeight * power
    }

    /**
     * Replacing the corresponding characters with whitespace or with empty character
     * */
    String escape(String text) {
        return text.replaceAll("[\r\n\t]", "").replaceAll(" +", " ")
    }

    private void drawDataGroup(String label) {
        contentStream.setFont(titleFont, fontGroupSize)
        contentStream.beginText()
        contentStream.newLineAtOffset(currentX, currentY as float)
        contentStream.showText(escape(label))
        contentStream.endText()
        newLine(1.5f)
    }

    private void drawTextField(LocalisedField localisedField, DataField dataField, int layoutX, int layoutY, int fieldWidth, int fieldHeight){
        //To test
        contentStream.setStrokingColor(Color.DARK_GRAY)
        contentStream.setLineWidth(0.5f)
        contentStream.addRect(currentX + (layoutX * formGridColWidth), currentY - (layoutY * formGridRowHeight) - fieldHeight, fieldWidth as float, fieldHeight as float)
        contentStream.stroke()

        drawDataFieldLabel(localisedField.name, layoutX, layoutY, fieldWidth, fieldHeight)
        drawTextValue((dataField.value ?: "") as String, layoutX, layoutY, fieldWidth, fieldHeight)
    }

    private void drawDataFieldLabel(String text, int layoutX, int layoutY, int fieldWidth, int fieldHeight){
        float textWidth = labelFont.getStringWidth(text) / 1000 * fontLabelSize
        String[] multiLineText = [text]
        int lineCounter = 1

        if(textWidth > fieldWidth * 0.5 - padding){
            multiLineText = getMultilineText(text, (fieldWidth - padding) / fontLabelSize)
        }

        for(String line : multiLineText){
            contentStream.setFont(labelFont, fontLabelSize)
            contentStream.beginText()
            contentStream.newLineAtOffset(currentX + (layoutX * formGridColWidth) + padding, currentY - (layoutY * formGridRowHeight) - lineHeight * lineCounter)
            contentStream.showText(line as String)
            contentStream.endText()
            lineCounter++
        }
    }

    private void drawTextValue(String text, int layoutX, int layoutY, int fieldWidth, int fieldHeight){
        float textWidth = labelFont.getStringWidth(text) / 1000 * fontValueSize
        String[] multiLineText = [text]
        int lineCounter = 1

        if(textWidth > fieldWidth * 0.5 - padding){
            multiLineText = getMultilineText(text, (fieldWidth * 0.85 - padding) / fontValueSize)
        }

        for(String line : multiLineText){
            contentStream.setFont(valueFont, fontValueSize)
            contentStream.beginText()
            contentStream.newLineAtOffset(currentX + layoutX * formGridColWidth + fieldWidth * 0.5, currentY - (layoutY * formGridRowHeight) - lineHeight * lineCounter)
            contentStream.showText(line as String)
            contentStream.endText()
            lineCounter++
        }
    }

    private String[] getMultilineText(String text, float maxLineLength){
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        StringBuilder output = new StringBuilder(text.length());
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
        return output.toString().split("\n");
    }

    private int countFieldWidth(LocalisedField field, DataGroup dataGroup){
        if(field.layout != null) {
            return field.layout.cols * formGridColWidth - 5
        }else{
            return (dataGroup.stretch ? (formGridColWidth * formGridCols) : (formGridColWidth * formGridCols / 2))
        }
    }

    private int countFieldHeight(LocalisedField field){
        if(field.layout != null) {
            return field.layout.rows * formGridRowHeight - padding
        }else{
            return formGridRowHeight
        }
    }
}
