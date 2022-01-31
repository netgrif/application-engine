package com.netgrif.application.engine.pdf.generator.service.interfaces;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.workflow.domain.Case;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface IPdfGenerator {
    void setupPdfGenerator(PdfResource pdfResource) throws IOException;

    void setupPdfGenerator(PdfResource pdfResource, float version) throws IOException;

    void addCustomField(PdfField field, PdfResource pdfResource) throws IOException;

    File generatePdf(Case formCase, String transitionId, PdfResource pdfResource, List<String> excludedFields);

    File generatePdf(Case formCase, String transitionId, PdfResource pdfResource) throws IOException;

    File generatePdf(PdfResource pdfResource) throws IOException;

    void generatePdf(Case formCase, String transitionId, PdfResource pdfResource, OutputStream stream);

    void generatePdf(Case formCase, Transition transition, PdfResource pdfResource, OutputStream stream);

    void generateData(PetriNet petriNet, Case useCase, Transition transition, PdfResource pdfResource, List<String> excludedFields);

    void generateData(PetriNet petriNet, Case useCase, Transition transition, PdfResource pdfResource);

    void generateData(PdfField pdfField, PdfResource pdfResource) throws IOException;
}