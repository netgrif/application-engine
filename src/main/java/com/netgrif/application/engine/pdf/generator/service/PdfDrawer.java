package com.netgrif.application.engine.pdf.generator.service;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.config.types.PdfBooleanFormat;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.application.engine.pdf.generator.service.renderer.*;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.Setter;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.netgrif.application.engine.pdf.generator.service.renderer.Renderer.removeUnsupportedChars;

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
    public void closeTemplate() throws IOException {
        if (templatePdf != null) {
            templatePdf.close();
            templatePdf = null;
        }
    }

    @Override
    public void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        PDPage emptyPage;
        PDFCloneUtility cloneUtility = new PDFCloneUtility(templatePdf);
        if (!isOnLastPage()) {
            currentPage = pageList.get(pageList.indexOf(currentPage) + 1);
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        } else if (templatePdf != null || isOnLastPage()) {
            if (templatePdf != null && pageList.size() == 0) {
                emptyPage = templatePdf.getPage(0);
            } else if (templatePdf != null && templatePdf.getPages().getCount() > 1) {
                COSDictionary dictionary = (COSDictionary) cloneUtility.cloneForNewDocument(templatePdf.getPage(1));
                emptyPage = new PDPage(dictionary);
            } else {
                emptyPage = new PDPage(resource.getPageSize());
            }
            pageList.add(emptyPage);
            pdf.addPage(emptyPage);
            currentPage = emptyPage;
            contentStream = new PDPageContentStream(pdf, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
        }
    }

    private boolean isOnLastPage() {
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

        for (PDPage page : pageList) {
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
    public void drawI18nDividerField(PdfField field) throws IOException {
        I18nDividerFieldRenderer i18nDividerFieldRenderer = new I18nDividerFieldRenderer();
        i18nDividerFieldRenderer.setupRenderer(this, resource);
        i18nDividerFieldRenderer.renderValue(field, 0);
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
            drawSvg(resource.getBooleanChecked(), x, y);
        } else {
            drawSvg(resource.getBooleanUnchecked(), x, y);
        }
    }

    @Override
    public boolean drawSelectionButton(List<String> values, String choice, int x, int y, FieldType fieldType) throws IOException {
        if (values.contains(choice)) {
            if (fieldType == FieldType.MULTICHOICE || fieldType == FieldType.MULTICHOICE_MAP) {
                drawSvg(resource.getCheckboxChecked(), x, y);
            } else if (fieldType == FieldType.ENUMERATION || fieldType == FieldType.ENUMERATION_MAP) {
                drawSvg(resource.getRadioChecked(), x, y);
            }
        } else {
            if (fieldType == FieldType.MULTICHOICE || fieldType == FieldType.MULTICHOICE_MAP) {
                drawSvg(resource.getCheckboxUnchecked(), x, y);
            } else if (fieldType == FieldType.ENUMERATION || fieldType == FieldType.ENUMERATION_MAP) {
                drawSvg(resource.getRadioUnchecked(), x, y);
            }
        }
        return true;
    }

    @Override
    public void drawStroke(int x, int y, int fieldPosY, int width, int lineCounter, float strokeWidth) throws IOException {
        int height = lineHeight * (lineCounter) - padding;
        contentStream.setStrokingColor(Color.LIGHT_GRAY);
        contentStream.setLineWidth(strokeWidth);
        if (fieldPosY >= marginBottom || height > 0) {
            width -= 50;
            y -= 8;
            x += 8;
            contentStream.moveTo(x, y);
            // bottom of rectangle, left to right
            contentStream.lineTo((float) (x + width), y);
            contentStream.curveTo(x + width + 5.9f, y + 0.14f,
                    x + width + 11.06f, y + 5.16f,
                    x + width + 10.96f, y + 10f);

            // right of rectangle, bottom to top
            contentStream.lineTo(x + width + 10.96f, (float) (y + height));
            contentStream.curveTo(x + width + 11.06f, y + height - 5.16f + 10,
                    x + width + 5.9f, y + height + 0.14f + 10,
                    (float) (x + width), y + height + 10f);

            // top of rectangle, right to left
            contentStream.lineTo(x, y + height + 10f);
            contentStream.curveTo(x - 5.9f, y + height + 0.14f + 10,
                    x - 11.06f, y + height - 5.16f + 10,
                    x - 10.96f, (float) (y + height));

            // left of rectangle, top to bottom
            contentStream.lineTo(x - 10.96f, y + 10f);
            contentStream.curveTo(x - 11.06f, y + 5.16f,
                    x - 5.9f, y + 0.14f,
                    x, y);

            contentStream.closePath();
        }
        contentStream.stroke();
    }

    @Override
    public void drawLine(int x, int y, int fieldPosY, int width, int lineCounter, float strokeWidth, Color color) throws IOException {
        contentStream.setStrokingColor(color);
        contentStream.moveTo(x, y);
        contentStream.lineTo((float) (x + width), y);
        contentStream.stroke();
    }

    @Override
    public void writeString(PDType0Font font, int fontSize, int x, int y, String text, Color color) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(color);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(removeUnsupportedChars(text, resource));
        contentStream.endText();
    }

    @Override
    public void writeLabel(PDType0Font font, int fontSize, int x, int y, String text, Color color) throws IOException {
        contentStream.setNonStrokingColor(color != null ? color : Color.GRAY);
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(removeUnsupportedChars(text, resource));
        contentStream.endText();
    }

    protected boolean checkBooleanValue(List<String> values, String text) {
        PdfBooleanFormat format = resource.getBooleanFormat();
        if (values.get(0).equals("true")) {
            if (!format.equals(PdfBooleanFormat.SINGLE_BOX_EN) && !format.equals(PdfBooleanFormat.SINGLE_BOX_SK)) {
                return format.getValue().get(0).equals(text);
            } else {
                return true;
            }
        } else if (format.equals(PdfBooleanFormat.DOUBLE_BOX_WITH_TEXT_EN) || format.equals(PdfBooleanFormat.DOUBLE_BOX_WITH_TEXT_SK)) {
            return format.getValue().get(1).equals(text);
        }
        return false;
    }

    protected void drawSvg(PDFormXObject resourceObject, int x, int y) throws IOException {
        contentStream.saveGraphicsState();
        AffineTransform transform = new AffineTransform(boxSize, 0.0F, 0.0F, boxSize, x, y - resource.getBoxPadding());
        contentStream.transform(new Matrix(transform));
        contentStream.drawForm(resourceObject);
        contentStream.restoreGraphicsState();
    }
}
