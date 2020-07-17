package com.netgrif.workflow.pdf.generator.service.interfaces;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;

public interface IPdfDrawer {
    void setupDrawer(PDDocument pdf);
    void newPage() throws IOException;
    void drawTitle(String title, int fieldWidth) throws IOException;
    void drawTextField(String label, String value, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException;
    int drawFieldLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, PDType0Font font, int fontSize) throws IOException;
    void drawTextValue(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException;
    void closeContentStream() throws IOException;
}
