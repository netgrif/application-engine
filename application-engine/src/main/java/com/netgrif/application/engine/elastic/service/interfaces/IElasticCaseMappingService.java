package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.workflow.domain.Case;

public interface IElasticCaseMappingService {
    ElasticCase transform(Case useCase);
}
