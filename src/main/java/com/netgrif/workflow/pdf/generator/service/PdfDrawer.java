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

    @Override
    public void drawTitle(String title, int fieldWidth) throws IOException {
        float textWidth = getTextWidth(title, titleFont, FONT_TITLE_SIZE);
        String[] multiLineText = new String[]{title};
        int lineCounter = 1, x = (int) (BASE_X + ((PAGE_DRAWABLE_WIDTH - textWidth) / 2 )), y;
        int maxLineSize = getMaxLabelLineSize(fieldWidth, FONT_TITLE_SIZE);

        if(textWidth > fieldWidth - 2 * PADDING){
            x = BASE_X;
            multiLineText = DataConverter.generateMultiLineText(title, maxLineSize);
        }

        for(String line : multiLineText){
            y = BASE_Y - LINE_HEIGHT * lineCounter;
            sendStringToContentStream(titleFont, FONT_TITLE_SIZE, x, y, line);
            lineCounter++;
        }
    }

    @Override
    public void drawTextField(String label, String value, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException {
        //To test
        int lineCounter = drawFieldLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, labelFont, FONT_LABEL_SIZE);
        drawTextValue(value, fieldX, fieldY, fieldWidth, fieldHeight, lineCounter);
    }

    @Override
    public int drawFieldLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, PDType0Font font, int fontSize) throws IOException {
        float textWidth = getTextWidth(text, font, fontSize);
        String[] multiLineText = new String[]{text};
        int lineCounter = 1, x, y;
        int maxLineSize = getMaxLabelLineSize(fieldWidth, fontSize);

        if(textWidth > fieldWidth - PADDING){
            multiLineText = DataConverter.generateMultiLineText(text, maxLineSize);
        }

        for(String line : multiLineText){
            x = fieldX + PADDING;
            y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
            sendStringToContentStream(font, fontSize, x, y, line);
            lineCounter++;
        }
        return lineCounter;
    }

    @Override
    public void drawTextValue(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException {
        float textWidth = getTextWidth(text, valueFont, FONT_VALUE_SIZE);
        String[] multiLineText = new String[]{text};
        int x, y;
        int maxLineSize = getMaxValueLineSize(fieldWidth);

        if(textWidth > fieldWidth - 2 * PADDING){
            multiLineText = DataConverter.generateMultiLineText(text, maxLineSize);
        }

        drawStroke(fieldX, fieldY, fieldWidth, fieldHeight - LINE_HEIGHT * (lineCounter - 1) - PADDING);

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

    private void drawStroke(int x, int y, int width, int height) throws IOException {
        contentStream.setStrokingColor(Color.GRAY);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();
    }

    public static int getMaxLabelLineSize(int fieldWidth, int fontSize){
        return (int) ((fieldWidth - PADDING) * 1.5 / fontSize);
    }

    public static int getMaxValueLineSize(int fieldWidth){
        return (int) ((fieldWidth - PADDING) * 1.5 / FONT_VALUE_SIZE);
    }

    public static int getTextWidth(String text, PDType0Font font, int fontSize) throws IOException {
        return (int) (font.getStringWidth(text) / 1000 * fontSize);
    }
}
