package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.util.List;

public interface IPdfDrawer {
    void setupDrawer(PDDocument pdf);
    void newPage() throws IOException;
    void drawTitle(String title, int fieldX, int fieldY, int fieldWidth) throws IOException;
    void closeContentStream() throws IOException;
    void drawTextField(String label, List<String> value, int fieldX, int fieldY, int fieldWidth, int fieldHeight) throws IOException;
    void drawSelectionField(String label, List<String> choices, List<String> values, int fieldX, int fieldY, int fieldWidth, int fieldHeight, FieldType type) throws IOException;
    int drawLabel(String text, int fieldX, int fieldY, int fieldWidth, int fieldHeight, PDType0Font font, int fontSize) throws IOException;
}
