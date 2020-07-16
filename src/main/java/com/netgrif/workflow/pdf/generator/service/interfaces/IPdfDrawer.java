package com.netgrif.workflow.pdf.generator.service.interfaces;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public interface IPdfDrawer {
    void setupDrawer(PDDocument pdf);
    void newPage() throws IOException;
    void drawDataGroup(String label, int dgX, int dgY) throws IOException;
    void drawTextField(String label, String value, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException;
    int drawDataFieldLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException;
    void drawTextValue(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, int lineCounter) throws IOException;
    void closeContentStream() throws IOException;
}
