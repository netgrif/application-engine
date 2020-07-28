package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A drawer service that is able to draw elements to a content stream
 */
@Service
public class PdfDrawer implements IPdfDrawer {

    private PDDocument pdf;

    private PDPageContentStream contentStream;

    private List<PDPage> pageList;

    private PDPage currentPage;

    @Autowired
    private PdfResource resource;

    private int marginLeft, marginBottom, marginTop;
    private int lineHeight, pageDrawableWidth, padding, pageHeight, baseX;
    private int fontValueSize, fontLabelSize, fontTitleSize;

    /**
     * Sets the PD document to the drawer
     *
     * @param pdf PD document that fields will be exported to
     */
    public void setupDrawer(PDDocument pdf) {
        this.pdf = pdf;
        this.pageList = new ArrayList<>();
        this.marginLeft = resource.getMarginLeft();
        this.marginBottom = resource.getMarginBottom();
        this.marginTop = resource.getMarginTop();
        this.lineHeight = resource.getLineHeight();
        this.pageDrawableWidth = resource.getPageDrawableWidth();
        this.padding = resource.getPadding();
        this.baseX = resource.getBaseX();
        this.pageHeight = resource.getPageHeight();
        this.fontValueSize = resource.getFontValueSize();
        this.fontLabelSize = resource.getFontLabelSize();
        this.fontTitleSize = resource.getFontTitleSize();
    }

    /**
     * Remotely closes the private content stream object
     *
     * @throws IOException I/O exception handling for operations with files
     */
    @Override
    public void closeContentStream() throws IOException {
        contentStream.close();
        contentStream = null;
    }

    /**
     * Creates new page whether it is a new document or just the bottom of current page is reached
     */
    @Override
    public void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        if (pageList.indexOf(currentPage) < pageList.size() - 1) {
            currentPage = pageList.get(pageList.indexOf(currentPage) + 1);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        } else {
            PDPage emptyPage = new PDPage(resource.getPageSize());
            pageList.add(emptyPage);
            pdf.addPage(emptyPage);
            currentPage = emptyPage;
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
            drawPageNumber(pageList.indexOf(emptyPage) + 1);
        }
    }

    /**
     * Checks whether there are any open pages. If so, closes all the open pages and opens the first page of the
     * document.
     *
     * @throws IOException I/O exception handling for operations with files
     */
    private void checkOpenPages() throws IOException {
        if (!currentPage.equals(pageList.get(0))) {
            contentStream.close();
            currentPage = pageList.get(0);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }
    }

    /**
     * Draws page number
     *
     * @param number number of current page
     * @throws IOException I/O exception handling for operations with files
     */
    private void drawPageNumber(int number) throws IOException {
        sendStringToContentStream(resource.getValueFont(), fontValueSize, marginLeft + (pageDrawableWidth / 2), marginBottom - 2 * lineHeight, String.valueOf(number));
    }

    /**
     * Draws the title of the document
     *
     * @param title      title of the document
     * @param fieldX     X position of title
     * @param fieldY     Y position of title
     * @param fieldWidth width of title
     * @throws IOException I/O exception handling for operations with files
     */
    @Override
    public void drawTitle(String title, int fieldX, int fieldY, int fieldWidth) throws IOException {
        float textWidth = getTextWidth(Collections.singletonList(title), resource.getTitleFont(), fontTitleSize);
        List<String> multiLineText = new ArrayList<>();
        int lineCounter = 0, x = (int) (baseX + ((pageDrawableWidth - textWidth) / 2)), y;
        int maxLineSize = getMaxLabelLineSize(fieldWidth, fontTitleSize);

        multiLineText.add(title);

        if (textWidth > fieldWidth - 2 * padding) {
            x = baseX;
            multiLineText = DataConverter.generateMultiLineText(Collections.singletonList(title), maxLineSize);
        }

        for (String line : multiLineText) {
            y = pageHeight - marginTop - fieldY - lineHeight * lineCounter;
            sendStringToContentStream(resource.getTitleFont(), fontTitleSize, x, y, line);
            lineCounter++;
        }
    }

    /**
     * Draws a text, text area, number, date and datetime fields
     *
     * @param label       label of field
     * @param values      list of values of field
     * @param fieldX      X position of field
     * @param fieldY      Y position of field
     * @param fieldWidth  width of field in PDF
     * @param fieldHeight height of field in PDF
     * @throws IOException I/O exception handling for operations with files
     */
    @Override
    public void drawTextField(String label, List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException {
        int lineCounter = drawLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, resource.getLabelFont(), fontLabelSize);
        drawTextValue(values, fieldX, fieldY, fieldWidth, fieldHeight, lineCounter);
    }

    /**
     * Draws a boolean field
     *
     * @param label       label of field
     * @param values      list of values of field
     * @param fieldX      X position of field
     * @param fieldY      Y position of field
     * @param fieldWidth  width of field in PDF
     * @param fieldHeight height of field in PDF
     * @throws IOException I/O exception handling for operations with files
     */
    @Override
    public void drawBooleanField(String label, List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException {
        int lineCounter = drawLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, resource.getLabelFont(), fontLabelSize);
        drawBooleanValue(values, fieldX, fieldY, fieldWidth, fieldHeight, lineCounter);
    }

    /**
     * Draws multi choice and enumeration fields
     *
     * @param label       label of field
     * @param choices     list of choices of field
     * @param values      list of values of field
     * @param fieldX      X position of field
     * @param fieldY      Y position of field
     * @param fieldWidth  width of field in PDF
     * @param fieldHeight height of field in PDF
     * @param type        type of field
     * @throws IOException I/O exception handling for operations with files
     */
    @Override
    public void drawSelectionField(String label, List<String> choices, List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight, FieldType type) throws IOException {
        int lineCounter = drawLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, resource.getLabelFont(), fontLabelSize);
        drawChoiceValue(choices, values, fieldX, fieldY, fieldWidth, fieldHeight, type, lineCounter);
    }

    /**
     * Counts the field width and available space for characters, split the text to multiple lines if needed and draws
     * the text to the content stream
     *
     * @param text        text to be drawn
     * @param fieldX      X position of field of label
     * @param fieldY      Y position of field of label
     * @param fieldWidth  width of field in PDF
     * @param fieldHeight height of field in PDF
     * @param font        font to be used
     * @param fontSize    size of font
     * @return count of lines that took for the drawer to draw the text
     * @throws IOException I/O exception handling for operations with files
     */
    @Override
    public int drawLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, PDType0Font font, int fontSize) throws IOException {
        float textWidth = getTextWidth(Collections.singletonList(text), font, fontSize);
        int maxLineSize = getMaxLabelLineSize(fieldWidth, fontSize);
        List<String> multiLineText = new ArrayList<String>() {{
            add(text);
        }};
        int linesOnPage = 0;
        int x = fieldX + padding, y = fieldY + fieldHeight - lineHeight;

        if (textWidth > fieldWidth - padding) {
            multiLineText = DataConverter.generateMultiLineText(Collections.singletonList(text), maxLineSize);
        }

        for (String line : multiLineText) {
            linesOnPage++;
            if (y < marginBottom) {
                fieldHeight -= lineHeight * (linesOnPage - 1);
                linesOnPage = 1;
                while (y < marginBottom) {
                    newPage();
                    fieldY = fieldY + pageHeight - marginTop - marginBottom - lineHeight;
                    y = fieldY + fieldHeight - lineHeight;
                }
            }
            y = fieldY + fieldHeight - lineHeight * linesOnPage;
            sendStringToContentStream(font, fontSize, x, y, line);
        }
        checkOpenPages();
        return multiLineText.size();
    }

    /**
     * Counts the field width and available space for characters, split the text to multiple lines if needed and draws
     * a text, text area, number, date and datetime field values to the content stream
     *
     * @param values      values to be drawn
     * @param fieldX      X position of field of values
     * @param fieldY      Y position of field of values
     * @param fieldWidth  width of field in PDF
     * @param fieldHeight height of field in PDF
     * @param lineCounter number of lines the drawer needed to draw the label of current field
     * @throws IOException I/O exception handling for operations with files
     */
    private void drawTextValue(List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException {
        float textWidth = getTextWidth(values, resource.getValueFont(), fontValueSize);
        int maxLineSize = getMaxValueLineSize(fieldWidth - 3 * padding);
        List<String> multiLineText = values;
        int x = fieldX + padding, y = fieldY + fieldHeight - lineHeight * lineCounter;
        int strokeLineCounter = 0;

        if (textWidth > fieldWidth - 3 * padding) {
            multiLineText = DataConverter.generateMultiLineText(values, maxLineSize);
        }

        for (String line : multiLineText) {
            lineCounter++;
            if (y < marginBottom) {
                drawStroke(fieldX, y, fieldY, fieldWidth, strokeLineCounter);
                fieldHeight -= lineHeight * (lineCounter - 1);
                lineCounter = 1;
                strokeLineCounter = 0;
                while (y < marginBottom) {
                    newPage();
                    fieldY = fieldY + pageHeight - marginTop - marginBottom - lineHeight;
                    y = fieldY + fieldHeight - lineHeight;
                }
            }
            y = fieldY + fieldHeight - lineHeight * lineCounter;
            strokeLineCounter++;
            sendStringToContentStream(resource.getValueFont(), fontValueSize, x, y, line);
        }
        drawStroke(fieldX, y, fieldY, fieldWidth, strokeLineCounter);
        checkOpenPages();
    }

    private void drawBooleanValue(List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException {
        int x = fieldX + padding, y = fieldY + fieldHeight - lineHeight * lineCounter;

        if (y < marginBottom) {
            fieldHeight -= lineHeight * (lineCounter);
            while (y < marginBottom) {
                newPage();
                fieldY = fieldY + pageHeight - marginTop - marginBottom - lineHeight;
                y = fieldY + fieldHeight - lineHeight;
            }
        }
        drawStroke(fieldX, y, fieldY, fieldWidth, 1);

        if (values.get(0).equals("true")) {
            contentStream.drawImage(resource.getCheckboxChecked(), x, y, fontLabelSize, fontLabelSize);
        } else {
            contentStream.drawImage(resource.getCheckboxUnchecked(), x, y, fontLabelSize, fontLabelSize);
        }
    }

    /**
     * Counts the field width and available space for characters, split the text to multiple lines if needed and draws
     * a multi choice or enumeration field choices to the content stream
     *
     * @param choices     choices of field
     * @param values      values of field
     * @param fieldX      X position of field of choices
     * @param fieldY      Y position of field of choices
     * @param fieldWidth  width of field of choices
     * @param fieldHeight height of field of choices
     * @param type        type of field of choices
     * @param lineCounter number of lines the drawer needed to draw the label of current field
     * @throws IOException I/O exception handling for operations with files
     */
    private void drawChoiceValue(List<String> choices, List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight, FieldType type, int lineCounter) throws IOException {
        int maxLineSize = getMaxValueLineSize(fieldWidth - 3 * padding);
        List<String> multiLineText;
        int x = fieldX + 4 * padding, y = fieldY + fieldHeight - lineHeight * lineCounter;

        for (String choice : choices) {
            boolean buttonDrawn = false;
            float textWidth = getTextWidth(Collections.singletonList(choice), resource.getValueFont(), fontValueSize);
            multiLineText = new ArrayList<String>() {{
                add(choice);
            }};

            if (textWidth > fieldWidth - 4 * padding) {
                multiLineText = DataConverter.generateMultiLineText(Collections.singletonList(choice), maxLineSize);
            }

            for (String line : multiLineText) {
                lineCounter++;
                if (y < marginBottom) {
                    fieldHeight -= lineHeight * (lineCounter - 1);
                    lineCounter = 1;
                    while (y < marginBottom) {
                        newPage();
                        fieldY = fieldY + pageHeight - marginTop - marginBottom - lineHeight;
                        y = fieldY + fieldHeight - lineHeight;
                    }
                }
                y = fieldY + fieldHeight - lineHeight * lineCounter;
                sendStringToContentStream(resource.getValueFont(), fontValueSize, x, y, line);
                if (!buttonDrawn) {
                    buttonDrawn = drawSelectionButtons(values, choice, fieldX + padding, y, type);
                }
            }
        }
        checkOpenPages();
    }

    /**
     * Drawing checkbox and radio button images based on current field
     *
     * @param values    values of field
     * @param choice    currently drawing choice
     * @param x         X position of choice
     * @param y         Y position of choice
     * @param fieldType type of field
     * @return true because current choice already has image drawn
     * @throws IOException I/O exception handling for operations with files
     */
    private boolean drawSelectionButtons(List<String> values, String choice, int x, int y, FieldType fieldType) throws IOException {
        if (values.contains(choice)) {
            if (fieldType == FieldType.MULTICHOICE) {
                contentStream.drawImage(resource.getCheckboxChecked(), x, y - 2, fontLabelSize, fontLabelSize);
            } else if (fieldType == FieldType.ENUMERATION) {
                contentStream.drawImage(resource.getRadioChecked(), x, y - 2, fontLabelSize, fontLabelSize);
            }
        } else {
            if (fieldType == FieldType.MULTICHOICE) {
                contentStream.drawImage(resource.getCheckboxUnchecked(), x, y - 2, fontLabelSize, fontLabelSize);
            } else if (fieldType == FieldType.ENUMERATION) {
                contentStream.drawImage(resource.getRadioUnchecked(), x, y - 2, fontLabelSize, fontLabelSize);
            }
        }
        return true;
    }

    /**
     * Drawing text to content stream
     *
     * @param font     font of text
     * @param fontSize size of font
     * @param x        X position of line of text
     * @param y        Y position of line of text
     * @param text     text to be drawn
     * @throws IOException I/O exception handling for operations with files
     */
    private void sendStringToContentStream(PDType0Font font, int fontSize, int x, int y, String text) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * Draws boxes around PDF fields
     *
     * @param x           X position of left bottom corner of a box
     * @param y           Y position of left bottom corner of a box
     * @param fieldPosY   current Y position of field
     * @param width       box width
     * @param lineCounter number of currently drawn lines of text
     * @throws IOException I/O exception handling for operations with files
     */
    private void drawStroke(int x, int y, int fieldPosY, int width, int lineCounter) throws IOException {
        int customHeight = lineHeight * lineCounter;
        contentStream.setStrokingColor(Color.GRAY);
        contentStream.setLineWidth(0.5f);
        if (fieldPosY < marginBottom && customHeight > 0) {
            contentStream.addRect(x - padding, y - padding, width, customHeight);
        } else if (fieldPosY >= marginBottom) {
            contentStream.addRect(x - padding, y - padding, width, lineHeight * (lineCounter));
        }
        contentStream.stroke();
    }

    private int getMaxLabelLineSize(int fieldWidth, int fontSize) {
        return (int) ((fieldWidth - padding) * 1.5 / fontSize);
    }

    private int getMaxValueLineSize(int fieldWidth) {
        return (int) ((fieldWidth - padding) * 1.5 / fontValueSize);
    }

    public static int getTextWidth(List<String> values, PDType0Font font, int fontSize) throws IOException {
        int result = 0;
        for (String value : values) {
            if (result < font.getStringWidth(value) / 1000 * fontSize)
                result = (int) (font.getStringWidth(value) / 1000 * fontSize);
        }
        return result;
    }
}
