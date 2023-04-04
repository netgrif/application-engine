package com.netgrif.application.engine.pdf.generator.service.interfaces;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IPdfDrawer {

    void setupDrawer(PDDocument pdf, PdfResource pdfResource);

    void setTemplatePdf(PDDocument pdf);

    void closeTemplate() throws IOException;

    void newPage() throws IOException;

    void checkOpenPages() throws IOException;

    void closeContentStream() throws IOException;

    PDPageContentStream getContentStream();

    void setContentStream(PDPageContentStream contentStream);

    List<PDPage> getPageList();

    PDDocument getPdf();

//    void drawTitleField(PdfField field) throws IOException;
//
//    void drawDataGroupField(PdfField field) throws IOException;

//    void drawTextField(PdfField<?> field) throws IOException;

//    void drawI18nDividerField(PdfField field) throws IOException;
//
//    void drawBooleanField(PdfField field) throws IOException;
//
//    void drawEnumerationField(PdfField field) throws IOException;
//
//    void drawMultiChoiceField(PdfField field) throws IOException;
//
//    void drawPageNumber() throws IOException;

    void drawBooleanBox(Boolean value, Map.Entry<Boolean, String> text, int x, int y) throws IOException;

    void drawLine(int x, int y, int fieldPosY, int width, int lineCounter, float strokeWidth, Color color) throws IOException;

    void writeString(PDType0Font font, int fontSize, int x, int y, String text, Color color) throws IOException;

    void writeLabel(PDType0Font font, int fontSize, int x, int y, String text, Color color) throws IOException;

    boolean drawSelectionButton(Set<String> values, String choice, int x, int y, String fieldType) throws IOException;

    void drawStroke(int x, int y, int fieldPosY, int width, int lineCounter, float strokeWidth) throws IOException;

}
