package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.auth.domain.Impersonator;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImpersonatorRepository extends CrudRepository<Impersonator, String> {

    Optional<Impersonator> findByImpersonatedId(String impersonatedId);

}
