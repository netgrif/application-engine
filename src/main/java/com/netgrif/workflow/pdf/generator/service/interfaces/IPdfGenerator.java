package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IPdfGenerator {
    File convertCustomField(List<PdfField> pdfFields, PdfResource pdfResource);


    File convertCaseForm(Case formCase, String transitionId, PdfResource pdfResource) throws IOException;

    File convertCaseForm(Case formCase, Map<String, DataGroup> dataGroupMap, PdfResource pdfResource) throws IOException;

    void convertCaseForm(Case formCase, String transitionId, PdfResource pdfResource, OutputStream stream) throws IOException;

    void convertCaseForm(Case formCase, Map<String, DataGroup> dataGroupMap, PdfResource pdfResource, OutputStream stream) throws IOException;

    void generateData(PetriNet petriNet, Map<String, DataGroup> dataGroupMap, Map<String, DataField> dataSet, PdfResource pdfResource) throws IOException;

    void generateData(List<PdfField> pdfFields, PdfResource pdfResource) throws IOException;
}