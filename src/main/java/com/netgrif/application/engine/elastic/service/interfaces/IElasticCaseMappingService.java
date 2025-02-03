package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.adapter.workflow.domain.Case;

public interface IElasticCaseMappingService {
    ElasticCase transform(Case useCase);
}
