package com.netgrif.application.engine.adapter.spring.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUserRef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnonymousUserRefRepository extends MongoRepository<AnonymousUserRef, String> {

    Optional<AnonymousUserRef> findByRealmId(String realmId);

}
