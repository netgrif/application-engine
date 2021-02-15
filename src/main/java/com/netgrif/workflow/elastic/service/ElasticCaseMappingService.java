package com.netgrif.workflow.elastic.service;


import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.stereotype.Service;

@Service
public class ElasticCaseMappingService implements IElasticCaseMappingService {
    @Override
    public ElasticCase transform(Case useCase) {
        return new ElasticCase(useCase);
    }
}
