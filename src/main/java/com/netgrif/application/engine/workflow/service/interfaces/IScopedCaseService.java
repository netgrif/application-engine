package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;

public interface IScopedCaseService {
    void save(Case scopedCase);
    void saveAll(Iterable<Case> scopedCase);
}
