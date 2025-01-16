package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TemplateCase;

public interface ITemplateCaseService {
    Case findLatestTemplateCase(String processIdentifier);

    Case findOne(String templateCaseId);

    void save(TemplateCase templateCase);
    void saveAll(Iterable<TemplateCase> templateCases);
}
