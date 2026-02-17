package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.adapter.spring.auth.domain.Realm;
import com.netgrif.application.engine.auth.realm.request.RealmSearch;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface RealmRepository extends MongoRepository<Realm, String> {

    @Query("{ '_id': ?0 }")
    Optional<Realm> findByName(String name);

    Optional<Realm> findByDefaultRealmTrue();

    @Query(value = "{}", fields = "{ 'id': 1, 'name': 1, 'defaultRealm': 1, 'adminRealm': 1, 'tenantId': 1 }")
    Page<Realm> findAllSmall(Pageable pageable);

    @Query("{ 'adminRealm': true }")
    Optional<Realm> findAdminRealm();

    @Query("{ 'defaultRealm': true, 'tenantId': ?0 }")
    Optional<Realm> findByDefaultRealmTrueAndTenantId(String tenantId);

    default Page<Realm> searchRealms(RealmSearch realmSerach, Pageable pageable, MongoTemplate mongoTemplate) {
        Criteria searchCriteria = new Criteria();

        List<Criteria> criteriaList = new ArrayList<>();

        if (realmSerach != null) {
            if (realmSerach.id() != null && !realmSerach.id().isBlank()) {
                criteriaList.add(Criteria.where("id").regex(realmSerach.id(), "i"));
            }
            if (realmSerach.name() != null && !realmSerach.name().isBlank()) {
                criteriaList.add(Criteria.where("name").regex(realmSerach.name(), "i"));
            }
            if (realmSerach.description() != null && !realmSerach.description().isBlank()) {
                criteriaList.add(Criteria.where("description").regex(realmSerach.description(), "i"));
            }
            if (Boolean.TRUE.equals(realmSerach.adminRealm())) {
                criteriaList.add(Criteria.where("adminRealm").is(true));
            }

            if (Boolean.TRUE.equals(realmSerach.defaultRealm())) {
                criteriaList.add(Criteria.where("defaultRealm").is(true));
            }

            if (Boolean.TRUE.equals(realmSerach.enableBlocking())) {
                criteriaList.add(Criteria.where("enableBlocking").is(true));
            }
        }

        if (!criteriaList.isEmpty()) {
            searchCriteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));   //Mozno and....
        }

        MatchOperation matchStage = Aggregation.match(searchCriteria);

        CountOperation countStage = Aggregation.count().as("totalCount");
        Aggregation countAggregation = Aggregation.newAggregation(matchStage, countStage);
        Document countResult = mongoTemplate.aggregate(countAggregation, "realm", Document.class).getUniqueMappedResult();
        long totalCount = countResult != null ? countResult.getInteger("totalCount") : 0;

        SortOperation sortStage = getSortOperation(pageable);

        SkipOperation skipStage = Aggregation.skip(pageable.getOffset());
        LimitOperation limitStage = Aggregation.limit(pageable.getPageSize());

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                sortStage,
                skipStage,
                limitStage
        );

        List<Realm> nodes = mongoTemplate.aggregate(aggregation, "realm", Realm.class).getMappedResults();

        return new PageImpl<>(nodes, pageable, totalCount);
    }

    private static SortOperation getSortOperation(Pageable pageable) {
        if (pageable.getSort().isUnsorted() || pageable.getSort().stream().anyMatch(order -> "undefined".equalsIgnoreCase(order.getProperty()))) {
            return Aggregation.sort(Sort.by(Sort.Direction.ASC, "id"));
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList();

        return Aggregation.sort(Sort.by(orders));
    }

}
