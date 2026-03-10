package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.Locale;

public interface IElasticPetriNetService {

    @Async
    void index(ElasticPetriNet net);

    void indexNow(ElasticPetriNet net);

    void remove(String id);

    Page<PetriNetReference> search(PetriNetSearch requests, Pageable pageable, Locale locale, Boolean isIntersection);

}
