package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;

public interface ITemplateCaseService {
    Case findLatestTemplateCase(String processIdentifier);

    Case findOne(String templateCaseId);

    void save(Case templateCase);
    void saveAll(Iterable<Case> templateCases);
}
