package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface IPdfGenerator {
    File convertCaseForm(Case formCase, String transitionId) throws IOException;

    void convertCaseForm(Case formCase, String transitionId, OutputStream stream) throws IOException;
}