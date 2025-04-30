package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuthorityRepository extends MongoRepository<Authority, String> {
    Authority findByName(String name);

    List<Authority> findAllByNameStartsWith(String prefix);

    List<Authority> findAllBy_idIn(List<ObjectId> ids);
}
