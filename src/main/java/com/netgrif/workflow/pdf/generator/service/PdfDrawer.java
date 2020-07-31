package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.config.types.PdfBooleanFormat;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.renderer.*;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import lombok.Setter;
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

    @Setter
    private PDDocument templatePdf;

    private PDPageContentStream contentStream;

    private List<PDPage> pageList;

    private PDPage currentPage = null;

    private PdfResource resource;

    private int marginBottom;
    private int lineHeight, padding;
    private int boxSize;

    public void setupDrawer(PDDocument pdf, PdfResource pdfResource) {
        this.pdf = pdf;
        this.resource = pdfResource;
        this.pageList = new ArrayList<>();
        this.marginBottom = resource.getMarginBottom();
        this.lineHeight = resource.getLineHeight();
        this.padding = resource.getPadding();
        this.boxSize = resource.getBoxSize();
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
        PDPage emptyPage;
        if (!isOnLastPage()) {
            currentPage = pageList.get(pageList.indexOf(currentPage) + 1);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        } else if (templatePdf != null || isOnLastPage()) {
            if (templatePdf != null && pageList.size() == 0) {
                emptyPage = templatePdf.getPage(0);
            } else if(templatePdf != null && templatePdf.getPages().getCount() > 1) {
                emptyPage = templatePdf.getPage(1);
            } else{
                emptyPage = new PDPage(resource.getPageSize());
            }
            pageList.add(emptyPage);
            pdf.addPage(emptyPage);
            currentPage = emptyPage;
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }
    }

    private boolean isOnLastPage(){
        return pageList.indexOf(currentPage) == pageList.size() - 1;
    }

    @Override
    public void checkOpenPages() throws IOException {
        if (!currentPage.equals(pageList.get(0))) {
            contentStream.close();
            currentPage = pageList.get(0);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }
    }

    @Override
    public void drawPageNumber() throws IOException {
        PageNumberRenderer pageNumberRenderer = new PageNumberRenderer();
        pageNumberRenderer.setupRenderer(this, resource);
        pageNumberRenderer.setFormat(resource.getPageNumberFormat());

        for(PDPage page : pageList){
            contentStream.close();
            contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true);
            pageNumberRenderer.renderPageNumber(pageList.indexOf(page) + 1, pageList.size());
        }
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
    public void drawBooleanBox(List<String> values, String text, int x, int y) throws IOException {
        if (checkBooleanValue(values, text)) {
            contentStream.drawImage(resource.getBooleanChecked(), x, y - resource.getBoxPadding(), boxSize, boxSize);
        } else {
            contentStream.drawImage(resource.getBooleanUnchecked(), x, y - resource.getBoxPadding(), boxSize, boxSize);
        }
    }

    @Override
    public boolean drawSelectionButton(List<String> values, String choice, int x, int y, FieldType fieldType) throws IOException {
        if (values.contains(choice)) {
            if (fieldType == FieldType.MULTICHOICE) {
                contentStream.drawImage(resource.getCheckboxChecked(), x, y - resource.getBoxPadding(), boxSize, boxSize);
            } else if (fieldType == FieldType.ENUMERATION) {
                contentStream.drawImage(resource.getRadioChecked(), x, y - resource.getBoxPadding(), boxSize, boxSize);
            }
        } else {
            if (fieldType == FieldType.MULTICHOICE) {
                contentStream.drawImage(resource.getCheckboxUnchecked(), x, y - resource.getBoxPadding(), boxSize, boxSize);
            } else if (fieldType == FieldType.ENUMERATION) {
                contentStream.drawImage(resource.getRadioUnchecked(), x, y - resource.getBoxPadding(), boxSize, boxSize);
            }
        }
        return true;
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

    @Override
    public void writeString(PDType0Font font, int fontSize, int x, int y, String text) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private boolean checkBooleanValue(List<String> values, String text){
        PdfBooleanFormat format = resource.getBooleanFormat();
        if (values.get(0).equals("true")) {
            if(!format.equals(PdfBooleanFormat.SINGLE_BOX_EN) && !format.equals(PdfBooleanFormat.SINGLE_BOX_SK)){
                return format.getValue().get(0).equals(text);
            }else{
                return true;
            }
        }else if(format.equals(PdfBooleanFormat.DOUBLE_BOX_WITH_TEXT_EN) || format.equals(PdfBooleanFormat.DOUBLE_BOX_WITH_TEXT_SK)){
            return format.getValue().get(1).equals(text);
        }
        return false;
    }
}
