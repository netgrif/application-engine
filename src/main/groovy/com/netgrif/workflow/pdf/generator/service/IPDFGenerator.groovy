package com.netgrif.workflow.pdf.generator.service

import com.netgrif.workflow.workflow.domain.Case
import org.springframework.stereotype.Service

@Service
interface IPDFGenerator {
    void convertCaseForm(Case formCase, String transitionId)
}