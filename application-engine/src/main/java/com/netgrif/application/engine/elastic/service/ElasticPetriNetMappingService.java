package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetMappingService;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import org.springframework.stereotype.Service;

@Service
public class ElasticPetriNetMappingService implements IElasticPetriNetMappingService {

    @Override
    public ElasticPetriNet transform(PetriNet net) {
        return new com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet(net);
    }
}
