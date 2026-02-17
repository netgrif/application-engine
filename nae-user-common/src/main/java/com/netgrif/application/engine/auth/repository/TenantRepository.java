package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.tenant.Tenant;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.by;

public interface TenantRepository extends MongoRepository<Tenant, String> {

    Optional<Tenant> findByOwner_Id(String ownerId);

    Optional<Tenant> findTenantByWorkspacesContaining(String workspaceId);

    Optional<Tenant> findTenantByRealmsContaining(String realmId);

    Optional<Tenant> findByWorkspacesContainingAndRealmsContaining(String workspaceId, String realmId);

    Optional<Tenant> findTenantByTenantCode(String tenantCode);

    boolean existsByWorkspacesContainingAndRealmsContaining(String workspaceId, String realmId);

    boolean existsByWorkspacesIn(List<String> workspaceIds);

    boolean existsByRealmsIn(List<String> realmIds);

    boolean existsByTenantCode(String tenantCode);

    boolean existsByWorkspacesInOrRealmsIn(List<String> workspaceIds, List<String> realmIds);

    List<Tenant> findTenantsByActiveIsTrue();

    List<Tenant> findTenantsBySuspendedIsTrue();

    List<Tenant> findTenantsByDeletedIsTrue();

    Page<Tenant> findAll(Pageable pageable);

    // todo maybe generic search for other resources
    default Page<Tenant> search(String search, Pageable pageable, MongoTemplate mongoTemplate) {
        Criteria criteria = new Criteria();
        if (search != null && !search.isBlank()) {
            criteria = new Criteria().orOperator(
                    Criteria.where("_id").regex(search, "i"),
                    Criteria.where("tenantCode").regex(search, "i"),
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("status").regex(search, "i")
            );
        }

        SortOperation sort = Aggregation.sort(pageable.getSort().isSorted()
                ? pageable.getSort()
                : by(Sort.Direction.ASC, "name"));

        FacetOperation facet = Aggregation.facet()
                .and(Aggregation.count().as("count")).as("totalCount")
                .and(sort, Aggregation.skip(pageable.getOffset()), Aggregation.limit(pageable.getPageSize())).as("results");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                facet
        );

        Document result = mongoTemplate.aggregate(aggregation, "tenant", Document.class)
                .getUniqueMappedResult();

        long totalCount = Optional.ofNullable(result)
                .map(doc -> doc.get("totalCount", List.class))
                .filter(list -> !list.isEmpty())
                .map(list -> ((Document) list.get(0)).getLong("count"))
                .orElse(0L);

        @SuppressWarnings("unchecked")
        List<Tenant> results = Optional.ofNullable(result)
                .map(doc -> doc.get("results", List.class))
                .orElse(List.of())
                .stream()
                .map(obj -> mongoTemplate.getConverter().read(Tenant.class, (Document) obj))
                .toList();

        return new PageImpl<>(results, pageable, totalCount);
    }
}
