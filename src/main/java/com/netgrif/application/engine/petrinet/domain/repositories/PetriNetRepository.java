package com.netgrif.application.engine.petrinet.domain.repositories;

import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.version.Version;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface PetriNetRepository extends MongoRepository<Process, String>, QuerydslPredicateExecutor<Process> {

    List<Process> findByTitle_DefaultValue(String title);

    Process findByImportId(String id);

    List<Process> findAllByIdentifier(String identifier);

    Process findByIdentifierAndVersion(String identifier, Version version);

    @Query("{'parentIdentifiers.id' :  ?0}")
    List<Process> findAllChildrenByParentId(ObjectId id);

    Page<Process> findByIdentifier(String identifier, Pageable pageable);

    Page<Process> findByIdentifierIn(List<String> identifier, Pageable pageable);

    List<Process> findAllByVersion(Version version);

    List<Process> findAllByUriNodeId(String uri);

    void deleteById(ObjectId id);
}