package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.DataField;

import java.util.List;
import java.util.Map;

public interface IDataConverter {

    void setPetriNet(PetriNet petriNet);
    void setDataGroups(Map<String, DataGroup> dataGroups);
    void setDataSet(Map<String, DataField> dataSet);
    void setPdfFields(List<PdfField> fields);
    List<PdfField> getPdfFields();

    void setupDataConverter(PdfResource resource);

    void generateTitleField();

    /**
     * Creates PdfField list that will be used for drawing elements to PDF
     */
    void generatePdfFields();

    /**
     * Adds data groups to PdfField list
     */
    void generatePdfDataGroups();

    /**
     * Checks whether there are any fields that needs to be shifted due to changed height
     */
    void correctFieldsPosition();
}
