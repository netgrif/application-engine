package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.auth.repository.AuthorityRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.dto.request.authority.AuthoritySearchRequestDto;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AuthorityServiceImpl implements AuthorityService {

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
        return authorityRepository.findById(s).orElseThrow(() -> new IllegalArgumentException("Authority with id " + s + " not found"));
    }

    @Override
    public Page<Authority> findAllByIds(Collection<String> ids, Pageable pageable) {
        return authorityRepository.findAllBy_idIn(ids.stream().map(ObjectId::new).collect(Collectors.toList()), pageable);
    }

    @Override
    public Page<Authority> search(AuthoritySearchRequestDto searchDto, Pageable pageable) {
        Query query;

        if (searchDto.fullText() != null && !searchDto.fullText().isBlank()) {
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("name").regex(searchDto.fullText(), "i")
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
