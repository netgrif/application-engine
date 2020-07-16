package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResources;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;

@Service
public class PdfDrawer extends PdfResources implements IPdfDrawer {

    private PDDocument pdf;

    private PDPageContentStream contentStream;

    private PDPage actualPage;

    public void setupDrawer(PDDocument pdf){
        this.pdf = pdf;
    }

    @Override
    public void closeContentStream() throws IOException {
        contentStream.close();
        contentStream = null;
    }

    /**
     * Creates new page whether it is a new document or just the bottom of current page is reached
     * */
    @Override
    public void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        actualPage = new PDPage(pageSize);
        pdf.addPage(actualPage);
        contentStream = new PDPageContentStream(pdf, actualPage, PDPageContentStream.AppendMode.APPEND, true, true);
    }

    /**
     * Replacing the corresponding characters with whitespace or with empty character
     * */
    String escape(String text) {
        return text.replaceAll("[\r\n\t]", "").replaceAll(" +", " ");
    }

    @Override
    public void drawDataGroup(String label, int dgX, int dgY) throws IOException {
        sendStringToContentStream(titleFont, FONT_GROUP_SIZE, dgX + PADDING, dgY + PADDING, escape(label));
    }

    @Override
    public void drawTextField(String label, String value, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException {
        //To test
        contentStream.setStrokingColor(Color.GRAY);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(fieldX, fieldY, fieldWidth, fieldHeight);
        contentStream.stroke();

        int lineCounter = drawDataFieldLabel(label, fieldX, fieldY, fieldWidth, fieldHeight);
        drawTextValue(value, fieldX, fieldY, fieldWidth, fieldHeight, lineCounter);
    }

    @Override
    public int drawDataFieldLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException {
        float textWidth = labelFont.getStringWidth(text) / 1000 * FONT_LABEL_SIZE;
        String[] multiLineText = new String[]{text};
        int lineCounter = 1, x, y;
        int maxLineSize = getMaxLabelLineSize(fieldWidth);

        if(textWidth > fieldWidth - PADDING){
            multiLineText = DataConverter.generateMultiLineText(text, maxLineSize);
        }

        for(String line : multiLineText){
            x = fieldX + PADDING;
            y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
            sendStringToContentStream(labelFont, FONT_LABEL_SIZE, x, y, line);
            lineCounter++;
        }
        return lineCounter;
    }

    @Override
    public void drawTextValue(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException {
        float textWidth = valueFont.getStringWidth(text) / 1000 * FONT_VALUE_SIZE;
        String[] multiLineText = new String[]{text};
        int x, y;
        int maxLineSize = getMaxValueLineSize(fieldWidth);

        if(textWidth > fieldWidth - 2 * PADDING){
            multiLineText = DataConverter.generateMultiLineText(text, maxLineSize);
        }

        for(String line : multiLineText){
            x = fieldX + 2 * PADDING;
            y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
            sendStringToContentStream(valueFont, FONT_VALUE_SIZE, x, y, line);
            lineCounter++;
        }
    }

    private void sendStringToContentStream(PDType0Font font, int fontSize, int x, int y, String text) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public static int getMaxLabelLineSize(int fieldWidth){
        return (fieldWidth - PADDING) / FONT_LABEL_SIZE;
    }

    public static int getMaxValueLineSize(int fieldWidth){
        return (int) ((fieldWidth - PADDING) * 1.5 / FONT_VALUE_SIZE);
    }
}
