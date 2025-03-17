package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.CaseRole;
import java.util.List;

public interface CaseRoleRepository {
    void removeAllByCaseId(String caseId);
    List<CaseRole> findAll();
}
