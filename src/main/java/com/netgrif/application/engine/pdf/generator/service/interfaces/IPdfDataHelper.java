package com.netgrif.application.engine.pdf.generator.service.interfaces;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.pdf.generator.domain.PdfField;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.Transition;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.List;

public interface IPdfDataHelper {

    void setPetriNet(PetriNet petriNet);

    void setTaskId(Case useCase, Transition transition);

    void setPdfFields(List<PdfField> fields);

    void setExcludedFields(List<String> excludedFields);

    List<PdfField> getPdfFields();

    void setupDataHelper(PdfResource resource);

    void generateTitleField();

    void generatePdfFields();

    void correctFieldsPosition();
}
