package com.netgrif.application.engine.pdf.generator.service.interfaces;

import com.netgrif.application.engine.pdf.generator.config.PdfResourceConfigurationProperties;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.Transition;
import com.netgrif.application.engine.objects.workflow.domain.Case;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface IPdfGenerator {
    void setupPdfGenerator(PdfResourceConfigurationProperties pdfResource) throws IOException;

    void setupPdfGenerator(PdfResourceConfigurationProperties pdfResource, float version) throws IOException;

    void addCustomField(PdfField field, PdfResourceConfigurationProperties pdfResource) throws IOException;

    File generatePdf(Case formCase, String transitionId, PdfResourceConfigurationProperties pdfResource, List<String> excludedFields);

    File generatePdf(Case formCase, String transitionId, PdfResourceConfigurationProperties pdfResource) throws IOException;

    File generatePdf(PdfResourceConfigurationProperties pdfResource) throws IOException;

    void generatePdf(Case formCase, String transitionId, PdfResourceConfigurationProperties pdfResource, OutputStream stream);

    void generatePdf(Case formCase, Transition transition, PdfResourceConfigurationProperties pdfResource, OutputStream stream);

    void generateData(PetriNet petriNet, Case useCase, Transition transition, PdfResourceConfigurationProperties pdfResource, List<String> excludedFields);

    void generateData(PetriNet petriNet, Case useCase, Transition transition, PdfResourceConfigurationProperties pdfResource);

    void generateData(PdfField pdfField, PdfResourceConfigurationProperties pdfResource) throws IOException;
}
