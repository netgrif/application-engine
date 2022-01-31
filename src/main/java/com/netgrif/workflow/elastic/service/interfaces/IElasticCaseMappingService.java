package com.netgrif.workflow.elastic.service.interfaces;

import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.workflow.domain.Case;

public interface IElasticCaseMappingService {
    ElasticCase transform(Case useCase);
}
