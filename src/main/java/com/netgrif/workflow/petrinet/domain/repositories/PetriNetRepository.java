package com.netgrif.workflow.petrinet.domain.repositories;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PetriNetRepository extends MongoRepository<PetriNet, String> {
    List<PetriNet> findByTitle(String title);

    PetriNet findByImportId(Long id);
}
