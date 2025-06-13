package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends MongoRepository<Authority, String> {
    Optional<Authority> findByName(String name);

    List<Authority> findAllByNameStartsWith(String prefix);

    Page<Authority> findAllBy_idIn(List<ObjectId> ids, Pageable pageable);
}
