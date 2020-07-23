package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

import java.io.IOException;

public interface IPdfGenerator {
    /**
     * Function is called when a PDF needs to be generated from the transition. This generate PDF from transition
     * data by calling corresponding functions.
     * @param formCase
     * @param transitionId
     * @throws IOException
     */
    void convertCaseForm(Case formCase, String transitionId) throws IOException;
}