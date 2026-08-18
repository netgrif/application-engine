package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.auth.config.AuthorityConfigurationProperties;
import com.netgrif.application.engine.auth.repository.AuthorityRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.dto.AuthoritySearchDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.objects.auth.domain.Authority.SCOPE_SUFFIX;

@Slf4j
public class AuthorityServiceImpl implements AuthorityService {

    private AuthorityRepository authorityRepository;
    private MongoTemplate mongoTemplate;
    private AuthorityConfigurationProperties authorityProperties;

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

    /**
     * Removes authority from database based on provided name if exists
     * @param name of authority to be deleted
     * */
    @Override
    public void delete(String name) {
        if (isScoped(name)) {
            throw new IllegalArgumentException("The authority name is not valid. Scope is suitable for this function.");
        }
        Optional<Authority> authority = authorityRepository.findByName(name);
        if (authority.isEmpty()) {
            log.warn("Authority with name [{}] not found", name);
            return;
        }
        authorityRepository.delete(authority.get());
    }

    /**
     * Returns authorities of provided scope. A scope contains authorities of the same name prefix, such as authorities
     * of PROCESS scope: PROCESS_UPLOAD, PROCESS_DELETE etc.
     * @param scope to be searched for
     * @return list of authorities of given scope
     * */
    @Override
    public List<Authority> findByScope(String scope) {
        List<Authority> authorities;
        if (scope.equals(SCOPE_SUFFIX))
            authorities = authorityRepository.findAll();
        else if (isScoped(scope)) {
            String prefix = scope.replace(SCOPE_SUFFIX, Strings.EMPTY);
            authorities = authorityRepository.findAllByNameStartsWith(prefix);
        } else {
            authorities = Collections.singletonList(findByName(scope));
        }
        return authorities;
    }

    /**
     * Returns authority based on name, throws exception if authority name is not valid or authority with provided name
     * cannot be found.
     * @param name of authority
     * @return authority object
     * */
    @Override
    public Authority findByName(String name) {
        if (isScoped(name)) {
            throw new IllegalArgumentException("The authority name is not valid. Scope is suitable for this function.");
        }
        Optional<Authority> authority = authorityRepository.findByName(name);
        if (authority.isEmpty()) {
            throw new IllegalArgumentException("Could not find authority with name [" + name + "]");
        }
        return authority.get();
    }


    /**
     * Returns authority from database based on provided ID
     * @param id of authority to be retrieved
     * @return optional of authority
     * */
    @Override
    public Optional<Authority> findOptionalByName(String id) {
        return authorityRepository.findByName(id);
    }

    /**
     * Returns the default authorities for simple user
     * @return set of authorities
     * */
    @Override
    @Cacheable("defaultUserAuthoritiesCache")
    public Set<Authority> getDefaultUserAuthorities() {
        return authorityProperties.getDefaultUserAuthorities().stream().map(this::findByScope).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Returns the default authorities for anonymous user
     * @return set of authorities
     * */
    @Override
    @Cacheable("defaultAnonymousAuthoritiesCache")
    public Set<Authority> getDefaultAnonymousAuthorities() {
        return authorityProperties.getDefaultAnonymousAuthorities().stream().map(this::findByScope).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Returns the default authorities for admin user
     * @return set of authorities
     * */
    @Override
    @Cacheable("defaultAdminAuthoritiesCache")
    public Set<Authority> getDefaultAdminAuthorities() {
        return authorityProperties.getDefaultAdminAuthorities().stream().map(this::findByScope).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Checks for authorityName, if it is valid scope name
     * @param authorityName of authority
     * @return boolean whether the provided name is valid scope name
     * */
    private boolean isScoped(String authorityName) {
        if (authorityName.contains(SCOPE_SUFFIX) && authorityName.indexOf(SCOPE_SUFFIX) != authorityName.length() - 1) {
            throw new IllegalArgumentException("The authority name or scope is not valid.");
        }
        return authorityName.endsWith(SCOPE_SUFFIX);
    }
}
