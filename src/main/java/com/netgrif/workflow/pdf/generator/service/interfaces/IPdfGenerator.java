package com.netgrif.workflow.pdf.generator.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

import java.io.IOException;

public interface IPdfGenerator {
    void convertCaseForm(Case formCase, String transitionId) throws IOException;
}