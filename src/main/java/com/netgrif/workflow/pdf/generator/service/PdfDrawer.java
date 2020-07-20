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
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfDrawer extends PdfResources implements IPdfDrawer {

    private PDDocument pdf;

    private PDPageContentStream contentStream;

    private List<PDPage> pageList;

    private PDPage currentPage;

    public void setupDrawer(PDDocument pdf){
        this.pdf = pdf;
        this.pageList = new ArrayList<>();
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
        if(pageList.indexOf(currentPage) < pageList.size() - 1){
            currentPage = pageList.get(pageList.indexOf(currentPage) + 1);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }else {
            PDPage emptyPage = new PDPage(pageSize);
            pageList.add(emptyPage);
            pdf.addPage(emptyPage);
            currentPage = emptyPage;
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
            drawPageNumber(pageList.indexOf(emptyPage) + 1);
        }
    }

    private void checkOpenPages() throws IOException {
        if(!currentPage.equals(pageList.get(0))){
            contentStream.close();
            PDPage firstPage = pageList.get(0);
            currentPage = firstPage;
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }
    }

    private void drawPageNumber(int number) throws IOException {
        sendStringToContentStream(valueFont, FONT_VALUE_SIZE, MARGIN_LEFT + (PAGE_DRAWABLE_WIDTH / 2), MARGIN_BOTTOM - 2 * LINE_HEIGHT, String.valueOf(number));
    }

    @Override
    public void drawTitle(String title, int fieldX, int fieldY, int fieldWidth) throws IOException {
        float textWidth = getTextWidth(title, titleFont, FONT_TITLE_SIZE);
        String[] multiLineText = new String[]{title};
        int lineCounter = 0, x = (int) (BASE_X + ((PAGE_DRAWABLE_WIDTH - textWidth) / 2 )), y;
        int maxLineSize = getMaxLabelLineSize(fieldWidth, FONT_TITLE_SIZE);

        if(textWidth > fieldWidth - 2 * PADDING){
            x = BASE_X;
            multiLineText = DataConverter.generateMultiLineText(title, maxLineSize);
        }

        for(String line : multiLineText){
            y = PAGE_HEIGHT - MARGIN_TOP - fieldY - LINE_HEIGHT * lineCounter;
            sendStringToContentStream(titleFont, FONT_TITLE_SIZE, x, y, line);
            lineCounter++;
        }
    }

    @Override
    public void drawTextField(String label, String value, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException {
        int lineCounter = drawFieldLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, labelFont, FONT_LABEL_SIZE);
        drawTextValue(value, fieldX, fieldY, fieldWidth, fieldHeight, lineCounter);
    }

    @Override
    public int drawFieldLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, PDType0Font font, int fontSize) throws IOException {
        float textWidth = getTextWidth(text, font, fontSize);
        int maxLineSize = getMaxLabelLineSize(fieldWidth, fontSize);
        String[] multiLineText = new String[]{text};
        int lineCounter = 1;
        int x, y;

        if(textWidth > fieldWidth - PADDING){
            multiLineText = DataConverter.generateMultiLineText(text, maxLineSize);
        }

        for(String line : multiLineText){
            x = fieldX + PADDING;
            y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
            if(y < MARGIN_BOTTOM + LINE_HEIGHT * multiLineText.length) {
                fieldHeight -= LINE_HEIGHT * (lineCounter - 1);
                lineCounter = 1;
                while (y < MARGIN_BOTTOM) {
                    newPage();
                    fieldY = fieldY + PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM - LINE_HEIGHT;
                    y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
                }
            }
            sendStringToContentStream(font, fontSize, x, y, line);
            lineCounter++;
        }
        checkOpenPages();
        return lineCounter;
    }

    @Override
    public void drawTextValue(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException {
        float textWidth = getTextWidth(text, valueFont, FONT_VALUE_SIZE);
        int maxLineSize = getMaxValueLineSize(fieldWidth);

        String[] multiLineText = new String[]{text};
        int x = fieldX + 2 * PADDING;
        int y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
        int strokeLineCounter = 0;
        lineCounter--;

        if(textWidth > fieldWidth - 2 * PADDING){
            multiLineText = DataConverter.generateMultiLineText(text, maxLineSize);
        }

        for(String line : multiLineText){
            lineCounter++;
            if(y < MARGIN_BOTTOM){
                drawStroke(fieldY, fieldX, y, fieldWidth, fieldHeight, strokeLineCounter);
                fieldHeight -= LINE_HEIGHT * (lineCounter - 1);
                lineCounter = 1;
                strokeLineCounter = 0;
                while (y < MARGIN_BOTTOM) {
                    newPage();
                    fieldY = fieldY + PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM - LINE_HEIGHT;
                    y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
                }
            }
            x = fieldX + 2 * PADDING;
            y = fieldY + fieldHeight - LINE_HEIGHT * lineCounter;
            strokeLineCounter++;
            sendStringToContentStream(valueFont, FONT_VALUE_SIZE, x, y, line);
        }
        drawStroke(fieldY, fieldX, y, fieldWidth, fieldHeight, strokeLineCounter);
        checkOpenPages();
    }

    private void sendStringToContentStream(PDType0Font font, int fontSize, int x, int y, String text) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawStroke(int fieldPosY, int x, int y, int width, int height, int lineCounter) throws IOException {
        int customHeight = LINE_HEIGHT * lineCounter;
        contentStream.setStrokingColor(Color.GRAY);
        contentStream.setLineWidth(0.5f);
        if(fieldPosY < MARGIN_BOTTOM && customHeight > 0) {
            contentStream.addRect(x, y - PADDING, width, customHeight);
        }else if(fieldPosY >= MARGIN_BOTTOM) {
            contentStream.addRect(x, y - PADDING, width, LINE_HEIGHT * (lineCounter));
        }
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
