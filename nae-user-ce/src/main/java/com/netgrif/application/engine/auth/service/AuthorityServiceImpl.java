package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.auth.repository.AuthorityRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AuthorityServiceImpl implements AuthorityService {

    private AuthorityRepository authorityRepository;

    @Autowired
    public void setAuthorityRepository(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
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
    public Page<Authority> findAllByIds(List<String> ids, Pageable pageable) {
        return authorityRepository.findAllBy_idIn(ids.stream().map(ObjectId::new).collect(Collectors.toList()), pageable);
    }
}
