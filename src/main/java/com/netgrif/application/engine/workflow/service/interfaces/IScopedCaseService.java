package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.ScopedCase;

public interface IScopedCaseService {
    void save(ScopedCase scopedCase);
    void saveAll(Iterable<ScopedCase> scopedCase);
}
