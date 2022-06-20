package com.netgrif.application.engine.petrinet.domain.repositories;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.version.Version;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface PetriNetRepository extends MongoRepository<PetriNet, String>, QuerydslPredicateExecutor<PetriNet> {

    List<PetriNet> findByTitle_DefaultValue(String title);

    PetriNet findByImportId(String id);

    List<PetriNet> findAllByIdentifier(String identifier);

    PetriNet findByIdentifierAndVersion(String identifier, Version version);

    Page<PetriNet> findByIdentifier(String identifier, Pageable pageable);

    Page<PetriNet> findByIdentifierIn(List<String> identifier, Pageable pageable);

    List<PetriNet> findAllByVersion(Version version);

    List<PetriNet> findAllByUriNodeId(String uri);

    void deleteBy_id(ObjectId id);
}