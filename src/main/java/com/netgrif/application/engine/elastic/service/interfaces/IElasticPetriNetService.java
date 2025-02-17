package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Locale;

public interface IElasticPetriNetService {

    @Async
    void index(ElasticPetriNet net);

    void indexNow(ElasticPetriNet net);

    void remove(String id);

    String findUriNodeId(PetriNet net);

    List<PetriNet> findAllByUriNodeId(String uriNodeId);

    Page<PetriNetReference> search(PetriNetSearch requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection);

}
