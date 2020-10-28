package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.workflow.domain.Case;

import java.util.List;

public interface IPdfDataHelper {

    void setPetriNet(PetriNet petriNet);
    void setTaskId(Case useCase, Transition transition);
    void setPdfFields(List<PdfField> fields);
    List<PdfField> getPdfFields();
    void setupDataHelper(PdfResource resource);
    void generateTitleField();
    void generatePdfFields();
    void correctFieldsPosition();
}
