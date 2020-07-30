package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.renderer.*;
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

    private int marginLeft, marginBottom;
    private int lineHeight, pageDrawableWidth, padding;
    private int fontValueSize, fontLabelSize;

    public void setupDrawer(PDDocument pdf) {
        this.pdf = pdf;
        this.pageList = new ArrayList<>();
        this.marginLeft = resource.getMarginLeft();
        this.marginBottom = resource.getMarginBottom();
        this.lineHeight = resource.getLineHeight();
        this.pageDrawableWidth = resource.getPageDrawableWidth();
        this.padding = resource.getPadding();
        this.fontValueSize = resource.getFontValueSize();
        this.fontLabelSize = resource.getFontLabelSize();
    }

    @Override
    public void closeContentStream() throws IOException {
        contentStream.close();
        contentStream = null;
    }

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

    @Override
    public void checkOpenPages() throws IOException {
        if (!currentPage.equals(pageList.get(0))) {
            contentStream.close();
            currentPage = pageList.get(0);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }
    }

    private void drawPageNumber(int number) throws IOException {
        writeString(resource.getValueFont(), fontValueSize, marginLeft + (pageDrawableWidth / 2), marginBottom - 2 * lineHeight, String.valueOf(number));
    }

    @Override
    public void drawTitleField(PdfField field) throws IOException {
        TitleRenderer titleFieldRenderer = new TitleRenderer();
        titleFieldRenderer.setupRenderer(this, resource);
        titleFieldRenderer.renderLabel(field);
    }

    @Override
    public void drawDataGroupField(PdfField field) throws IOException {
        DataGroupFieldRenderer dataGroupRenderer = new DataGroupFieldRenderer();
        dataGroupRenderer.setupRenderer(this, resource);
        dataGroupRenderer.renderLabel(field);
    }

    @Override
    public void drawTextField(PdfField field) throws IOException {
        TextFieldRenderer textFieldRenderer = new TextFieldRenderer();
        textFieldRenderer.setupRenderer(this, resource);
        int lineCounter = textFieldRenderer.renderLabel(field);
        textFieldRenderer.renderValue(field, lineCounter);
    }

    @Override
    public void drawBooleanField(PdfField field) throws IOException {
        BooleanFieldRenderer booleanFieldRenderer = new BooleanFieldRenderer();
        booleanFieldRenderer.setupRenderer(this, resource);
        int lineCounter = booleanFieldRenderer.renderLabel(field);
        booleanFieldRenderer.renderValue(field, lineCounter);
    }

    @Override
    public void drawEnumerationField(PdfField field) throws IOException {
        EnumerationRenderer enumerationRenderer = new EnumerationRenderer();
        enumerationRenderer.setupRenderer(this, resource);
        int lineCounter = enumerationRenderer.renderLabel(field);
        enumerationRenderer.renderValue(field, lineCounter);
    }

    @Override
    public void drawMultiChoiceField(PdfField field) throws IOException {
        MultiChoiceRenderer multiChoiceRenderer = new MultiChoiceRenderer();
        multiChoiceRenderer.setupRenderer(this, resource);
        int lineCounter = multiChoiceRenderer.renderLabel(field);
        multiChoiceRenderer.renderValue(field, lineCounter);
    }

    @Override
    public void drawBooleanBox(List<String> values, int x, int y) throws IOException {
        if (values.get(0).equals("true")) {
            contentStream.drawImage(resource.getCheckboxChecked(), x, y, fontLabelSize, fontLabelSize);
        } else {
            contentStream.drawImage(resource.getCheckboxUnchecked(), x, y, fontLabelSize, fontLabelSize);
        }
    }

    @Override
    public boolean drawSelectionButtons(List<String> values, String choice, int x, int y, FieldType fieldType) throws IOException {
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

    @Override
    public void writeString(PDType0Font font, int fontSize, int x, int y, String text) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    @Override
    public void drawStroke(int x, int y, int fieldPosY, int width, int lineCounter, float strokeWidth) throws IOException {
        int customHeight = lineHeight * lineCounter;
        contentStream.setStrokingColor(Color.GRAY);
        contentStream.setLineWidth(strokeWidth);
        if (fieldPosY < marginBottom && customHeight > 0) {
            contentStream.addRect(x - padding, y - padding, width, customHeight);
        } else if (fieldPosY >= marginBottom) {
            contentStream.addRect(x - padding, y - padding, width, lineHeight * (lineCounter));
        }
        contentStream.stroke();
    }
}
