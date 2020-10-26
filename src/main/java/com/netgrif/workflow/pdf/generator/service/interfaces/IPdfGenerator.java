package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.DataFieldLogic;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IPdfGenerator {
    void setupPdfGenerator(PdfResource pdfResource) throws IOException;

    void setupPdfGenerator(PdfResource pdfResource, float version) throws IOException;

    void addCustomField(PdfField field, PdfResource pdfResource) throws IOException;

    File generatePdf(Case formCase, String transitionId, PdfResource pdfResource) throws IOException;

    File generatePdf(PdfResource pdfResource) throws IOException;

    void generatePdf(Case formCase, String transitionId, PdfResource pdfResource, OutputStream stream) throws IOException;

    void generatePdf(Case formCase, Transition transition, Map<String, DataGroup> dataGroupMap, Map<String, DataFieldLogic> dataSetMap, PdfResource pdfResource, OutputStream stream);

    void generateData(PetriNet petriNet, Transition transition, Map<String, DataGroup> dataGroupMap, Map<String, DataField> dataSet, Map<String, DataFieldLogic> transDataSet, PdfResource pdfResource);

    void generateData(PdfField pdfField, PdfResource pdfResource) throws IOException;
}