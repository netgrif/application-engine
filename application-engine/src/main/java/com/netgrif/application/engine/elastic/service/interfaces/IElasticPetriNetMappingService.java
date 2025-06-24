package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;

public interface IElasticPetriNetMappingService {
    ElasticPetriNet transform(PetriNet net);
}
