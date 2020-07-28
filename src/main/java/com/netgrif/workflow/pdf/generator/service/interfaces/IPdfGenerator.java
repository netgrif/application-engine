package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;

import java.io.File;
import java.io.IOException;

public interface IPdfGenerator {
    File convertCaseForm(Case formCase, String transitionId) throws IOException;
}