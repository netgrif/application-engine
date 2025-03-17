package com.netgrif.application.engine.authentication.domain.repositories;

import com.netgrif.application.engine.authentication.domain.Identity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityRepository extends MongoRepository<Identity, String> {
    Optional<Identity> findByUsername(String username);
}
