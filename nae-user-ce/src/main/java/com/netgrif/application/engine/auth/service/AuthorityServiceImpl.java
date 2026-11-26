package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.auth.repository.AuthorityRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.dto.AuthoritySearchDto;
import com.netgrif.application.engine.objects.utils.TtlCache;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AuthorityServiceImpl implements AuthorityService {

    /**
     * Authorities are resolved by id every time a user, group or logged user is converted (one query per authority),
     * so they are cached briefly. They are only ever created by this service, so entries cannot go stale except by
     * deletion outside of it, which the TTL bounds.
     */
    private final TtlCache<String, Authority> authorityByIdCache = new TtlCache<>(Duration.ofSeconds(30), 1_000);

    private AuthorityRepository authorityRepository;
    private MongoTemplate mongoTemplate;

    @Autowired
    public void setAuthorityRepository(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Authority> findAll(Pageable pageable) {
        return authorityRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Authority getOrCreate(String name) {
        Optional<Authority> authority = authorityRepository.findByName(name);
        return authority.orElseGet(() -> authorityRepository.save(new AuthorityImpl(name)));
    }

    @Override
    public Authority getOne(String s) {
        Authority authority = authorityByIdCache.get(s, id -> authorityRepository.findById(id).orElse(null));
        if (authority == null) {
            throw new IllegalArgumentException("Authority with id " + s + " not found");
        }
        return authority;
    }

    @Override
    public Page<Authority> findAllByIds(Collection<String> ids, Pageable pageable) {
        return authorityRepository.findAllBy_idIn(ids.stream().map(ObjectId::new).collect(Collectors.toList()), pageable);
    }

    @Override
    public Page<Authority> search(AuthoritySearchDto searchDto, Pageable pageable) {
        Query query;

        if (searchDto.getFullText() != null && !searchDto.getFullText().isBlank()) {
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("name").regex(searchDto.getFullText(), "i")
            );
            query = Query.query(criteria);
        } else {
            query = Query.query(new Criteria());
        }
        long count = mongoTemplate.count(query, Authority.class);
        List<Authority> authorities = mongoTemplate.find(query.with(pageable), Authority.class);
        return new PageImpl<>(authorities, pageable, count);
    }
}
