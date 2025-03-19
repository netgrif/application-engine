package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.core.elastic.domain.ElasticPetriNet;
import com.netgrif.core.petrinet.domain.PetriNet;

public interface IElasticPetriNetMappingService {
    ElasticPetriNet transform(PetriNet net);
}
