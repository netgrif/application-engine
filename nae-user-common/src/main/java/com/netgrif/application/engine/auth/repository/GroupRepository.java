package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupRepository extends MongoRepository<Group, String>, QuerydslPredicateExecutor<Group> {
    Optional<Group> findByOwnerId(String id);

    Optional<Group> findByIdentifier(String identifier);

    Set<Group> findAllByMemberIdsContains(String memberId);

    Page<Group> findAllByIdIn(Set<String> ids, Pageable pageable);

    Page<Group> findAllByRealmId(String realmId, Pageable pageable);

    void removeAllByRealmIdIn(Collection<String> realmIds);

    void removeAllByRealmId(String realmId);
}
