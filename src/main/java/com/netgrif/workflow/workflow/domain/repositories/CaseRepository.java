package com.netgrif.workflow.workflow.domain.repositories;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CaseRepository extends MongoRepository<Case, String> {

    Page<Case> findAllByAuthor(Long authorId, Pageable pageable);

    List<Case> findAllByPetriNetId(String id);
}
