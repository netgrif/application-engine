package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.core.elastic.domain.ElasticCase;
import com.netgrif.core.workflow.domain.Case;

public interface IElasticCaseMappingService {
    ElasticCase transform(Case useCase);
}
