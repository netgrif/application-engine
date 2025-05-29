package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.petrinet.domain.Process;

public interface IElasticPetriNetMappingService {
    ElasticPetriNet transform(Process net);
}
