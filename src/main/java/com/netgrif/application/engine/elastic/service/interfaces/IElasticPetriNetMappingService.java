package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.adapter.petrinet.domain.PetriNet;

public interface IElasticPetriNetMappingService {
    ElasticPetriNet transform(PetriNet net);
}
